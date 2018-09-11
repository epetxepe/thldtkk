package fi.thl.thldtkk.api.metadata.controller;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.InstanceVariable;
import fi.thl.thldtkk.api.metadata.service.EditorStudyService;
import fi.thl.thldtkk.api.metadata.service.PublicStudyService;
import fi.thl.thldtkk.api.metadata.service.csv.CsvFileNameBuilder;
import fi.thl.thldtkk.api.metadata.service.csv.GeneratorResult;
import fi.thl.thldtkk.api.metadata.service.csv.InstanceVariableCsvGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.util.UUID;

import static fi.thl.thldtkk.api.metadata.util.spring.exception.NotFoundException.entityNotFound;

@Controller
@RequestMapping(API.PATH_WITH_VERSION + "")
public class InstanceVariableCsvExportController {

  @Autowired
  private PublicStudyService publicStudyService;
  @Autowired
  private EditorStudyService editorStudyService;

  @RequestMapping(
    value = "/public/studies/{studyId}/datasets/{datasetId}/instanceVariables.csv",
    method = RequestMethod.GET)
  public @ResponseBody byte[] publicInstanceVariablesAsCsv(
    @PathVariable("studyId") UUID studyId,
    @PathVariable("datasetId") UUID datasetId,
    @RequestParam(value = "lang", defaultValue = "en") String language,
    @RequestParam(value = "encoding", defaultValue = "ISO-8859-15") String encoding,
    HttpServletResponse response
  ) {
    Dataset dataset = publicStudyService.getDataset(studyId, datasetId)
      .orElseThrow(entityNotFound(Dataset.class, datasetId));
    return instanceVariablesAsCsv(dataset, language, encoding, response);
  }

  private byte[] instanceVariablesAsCsv(Dataset dataset, String language, String encoding, HttpServletResponse response) {
    for (InstanceVariable instanceVariable : dataset.getInstanceVariables()) {
      instanceVariable.setDataset(dataset);
    }
    InstanceVariableCsvGenerator generator = new InstanceVariableCsvGenerator(dataset.getInstanceVariables(), language, encoding);
    GeneratorResult result = generator.generate();

    if (result.getData().isPresent()) {
      String fileName = CsvFileNameBuilder.getInstanceVariableExportFileName(dataset, language);
      response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".csv");
      response.setHeader("Content-Type", "text/csv; charset=" + encoding);
      return result.getData().get();
    } else {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return new byte[0];
    }
  }

  @RequestMapping(
    value = "/editor/studies/{studyId}/datasets/{datasetId}/instanceVariables.csv",
    method = RequestMethod.GET)
  public @ResponseBody byte[] editorInstanceVariablesAsCsv(
    @PathVariable("studyId") UUID studyId,
    @PathVariable("datasetId") UUID datasetId,
    @RequestParam(value = "lang", defaultValue = "en") String language,
    @RequestParam(value = "encoding", defaultValue = "ISO-8859-15") String encoding,
    HttpServletResponse response
  ) {
    Dataset dataset = editorStudyService.getDataset(studyId, datasetId)
      .orElseThrow(entityNotFound(Dataset.class, datasetId));
    return instanceVariablesAsCsv(dataset, language, encoding, response);
  }

  @RequestMapping(
          value = "/editor/exampleImportVariables.xls",
          method = RequestMethod.GET)
  public @ResponseBody HttpEntity<byte[]> editorExampleInstanceVariablesCsv(
          @RequestParam(value = "encoding", defaultValue = "ISO-8859-15") String encoding
  ) throws IOException, URISyntaxException {
    return editorStudyService.getExampleInstanceVariablesCsv(encoding);
  }
}
