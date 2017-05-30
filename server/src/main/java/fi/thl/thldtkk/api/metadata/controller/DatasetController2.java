package fi.thl.thldtkk.api.metadata.controller;

import fi.thl.thldtkk.api.metadata.domain.Dataset;
import fi.thl.thldtkk.api.metadata.domain.InstanceVariable;
import fi.thl.thldtkk.api.metadata.service.Service;

import static com.google.common.base.MoreObjects.firstNonNull;
import fi.thl.thldtkk.api.metadata.util.spring.annotation.GetJsonMapping;
import fi.thl.thldtkk.api.metadata.util.spring.annotation.PostJsonMapping;
import fi.thl.thldtkk.api.metadata.util.spring.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static fi.thl.thldtkk.api.metadata.util.MapUtils.index;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/datasets")
public class DatasetController2 {

    @Autowired
    private Service<UUID, Dataset> datasetService;

    @GetJsonMapping
    public List<Dataset> queryDatasets() {
        return datasetService.query().collect(toList());
    }

    @GetJsonMapping("/{datasetId}")
    public Dataset getDataset(@PathVariable("datasetId") UUID datasetId) {
        return datasetService.get(datasetId).orElseThrow(NotFoundException::new);
    }

    @GetJsonMapping("/{datasetId}/instanceVariables")
    public List<InstanceVariable> getDatasetInstanceVariables(
            @PathVariable("datasetId") UUID datasetId) {
        Dataset dataset = datasetService.get(datasetId).orElseThrow(
                NotFoundException::new);
        return dataset.getInstanceVariables();
    }

    @GetJsonMapping("/{datasetId}/instanceVariables/{instanceVariableId}")
    public InstanceVariable getDatasetInstanceVariable(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("instanceVariableId") UUID instanceVariableId) {

        Dataset dataset = datasetService.get(datasetId).orElseThrow(
                NotFoundException::new);

        return dataset.getInstanceVariables().stream()
                .filter(v -> v.getId().equals(instanceVariableId))
                .findFirst().orElseThrow(NotFoundException::new);
    }

    @PostJsonMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    public Dataset postDataset(@RequestBody Dataset dataset,
            @RequestParam(name = "saveInstanceVariables", defaultValue = "true") boolean saveInstanceVariables) {

        Optional<Dataset> oldDataset = Optional.empty();

        if (dataset.getId() != null) {
            oldDataset = datasetService.get(dataset.getId());
        }

        if (!saveInstanceVariables && oldDataset.isPresent()) {
            return datasetService.save(new Dataset(dataset, oldDataset.get()
                    .getInstanceVariables()));
        }

        return datasetService.save(dataset);
    }

    @PostJsonMapping(path = "/{datasetId}/instanceVariables",
            produces = APPLICATION_JSON_UTF8_VALUE)
    public Dataset postDatasetInstanceVariable(
            @PathVariable("datasetId") UUID datasetId,
            @RequestBody InstanceVariable newVariable) {

        Dataset dataset = datasetService.get(datasetId).orElseThrow(NotFoundException::new);

        newVariable.setId(firstNonNull(newVariable.getId(), randomUUID()));

        Map<UUID, InstanceVariable> variablesById =
            index(dataset.getInstanceVariables(), InstanceVariable::getId);
        variablesById.put(newVariable.getId(), newVariable);

        return datasetService.save(new Dataset(dataset, new ArrayList<>(variablesById.values())));
    }

    @DeleteMapping("/{datasetId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteDataset(@PathVariable("datasetId") UUID datasetId) {
        datasetService.delete(datasetId);
    }

    @DeleteMapping("/{datasetId}/instanceVariables/{instanceVariableId}")
    @ResponseStatus(NO_CONTENT)
    public void deleteDatasetInstanceVariable(
            @PathVariable("datasetId") UUID datasetId,
            @PathVariable("instanceVariableId") UUID instanceVariableId) {

        Dataset dataset = datasetService.get(datasetId).orElseThrow(
                NotFoundException::new);

        List<InstanceVariable> variables = new ArrayList<>();
        variables.addAll(dataset.getInstanceVariables());
        variables.removeIf(v -> v.getId().equals(instanceVariableId));

        datasetService.save(new Dataset(dataset, variables));

    }
}
