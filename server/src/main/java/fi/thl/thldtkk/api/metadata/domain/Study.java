package fi.thl.thldtkk.api.metadata.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.thldtkk.api.metadata.SecurityClassification;
import fi.thl.thldtkk.api.metadata.domain.termed.Node;
import fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings;
import fi.thl.thldtkk.api.metadata.domain.termed.StrictLangValue;
import fi.thl.thldtkk.api.metadata.validator.ContainsAtLeastOneNonBlankValue;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toBoolean;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toLangValueMap;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toLocalDate;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toOptionalString;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toPropertyValue;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.toPropertyValues;
import static fi.thl.thldtkk.api.metadata.domain.termed.PropertyMappings.valuesToEnumCollection;
import static java.util.Objects.requireNonNull;

public class Study implements NodeEntity {

  public static final String TERMED_NODE_CLASS = "Study";

  private UUID id;
  private Date lastModifiedDate;
  private Boolean published;
  @ContainsAtLeastOneNonBlankValue
  private Map<String, String> prefLabel = new LinkedHashMap<>();
  private Map<String, String> altLabel = new LinkedHashMap<>();
  private Map<String, String> abbreviation = new LinkedHashMap<>();
  private Map<String, String> description = new LinkedHashMap<>();
  private Map<String, String> usageConditionAdditionalInformation = new LinkedHashMap<>();
  private LocalDate referencePeriodStart;
  private LocalDate referencePeriodEnd;
  private LocalDate collectionStartDate;
  private LocalDate collectionEndDate;
  private String numberOfObservationUnits;
  private Map<String, String> freeConcepts = new LinkedHashMap<>();
  private Boolean personRegistry;
  private Map<String, String> registryPolicy = new LinkedHashMap<>();
  private Map<String, String> purposeOfPersonRegistry = new LinkedHashMap<>();
  private Map<String, String> personRegistrySources = new LinkedHashMap<>();
  private Map<String, String> personRegisterDataTransfers = new LinkedHashMap<>();
  private Map<String, String> personRegisterDataTransfersOutsideEuOrEea = new LinkedHashMap<>();
  private ConfidentialityClass confidentialityClass;
  private Map<String, String> groundsForConfidentiality = new LinkedHashMap<>();
  private SecurityClassification securityClassification;
  private List<PrincipleForPhysicalSecurity> principlesForPhysicalSecurity = new ArrayList<>();
  private List<PrincipleForDigitalSecurity> principlesForDigitalSecurity = new ArrayList<>();
  private LocalDate dataProcessingStartDate;
  private LocalDate dataProcessingEndDate;
  private RetentionPolicy retentionPolicy;
  private Map<String, String> retentionPeriod = new LinkedHashMap<>();
  private Map<String, String> groundsForRetention = new LinkedHashMap<>();
  private Map<String, String> nationalArchivesFinlandArchivalDecision = new LinkedHashMap<>();
  private List<ExistenceForm> existenceForms = new ArrayList<>();
  private Map<String, String> physicalLocation = new LinkedHashMap<>();
  private String comment;
  private String externalId;

  private UserProfile lastModifiedByUser;
  private Organization ownerOrganization;
  private OrganizationUnit ownerOrganizationUnit;
  @Valid
  private List<PersonInRole> personInRoles = new ArrayList<>();
  private List<Link> links = new ArrayList<>();
  private UsageCondition usageCondition;
  private Universe universe;
  private Population population;
  private UnitType unitType;
  private LifecyclePhase lifecyclePhase;
  private List<DatasetType> datasetTypes = new ArrayList<>();
  private List<Concept> conceptsFromScheme = new ArrayList<>();
  private StudyGroup studyGroup;
  private List<Dataset> datasets = new ArrayList<>();
  private List<Study> predecessors = new ArrayList<>();
  private List<Study> successors = new ArrayList<>();
  @Valid
  private List<SystemInRole> systemInRoles = new ArrayList<>();
  private Boolean changedAfterPublish;

  /**
   * Required by GSON deserialization.
   */
  private Study() {

  }

  public Study(UUID id) {
    this.id = requireNonNull(id);
  }

  public Study(Node node) {
    this(node.getId());

    checkArgument(Objects.equals(node.getTypeId(), TERMED_NODE_CLASS));

    this.lastModifiedDate = node.getLastModifiedDate();
    this.published = toBoolean(node.getProperties("published"), false);
    this.prefLabel = toLangValueMap(node.getProperties("prefLabel"));
    this.altLabel = toLangValueMap(node.getProperties("altLabel"));
    this.abbreviation = toLangValueMap(node.getProperties("abbreviation"));
    this.description = toLangValueMap(node.getProperties("description"));
    this.usageConditionAdditionalInformation = toLangValueMap(node.getProperties("usageConditionAdditionalInformation"));
    this.referencePeriodStart = toLocalDate(node.getProperties("referencePeriodStart"), null);
    this.referencePeriodEnd = toLocalDate(node.getProperties("referencePeriodEnd"), null);
    this.collectionStartDate = toLocalDate(node.getProperties("collectionStartDate"), null);
    this.collectionEndDate = toLocalDate(node.getProperties("collectionEndDate"), null);
    this.numberOfObservationUnits = PropertyMappings.toString(node.getProperties("numberOfObservationUnits"));
    this.freeConcepts = toLangValueMap(node.getProperties("freeConcepts"));
    this.personRegistry = PropertyMappings.toBoolean(node.getProperties("personRegistry"), null);
    this.registryPolicy = toLangValueMap(node.getProperties("registryPolicy"));
    this.purposeOfPersonRegistry = toLangValueMap(node.getProperties("purposeOfPersonRegistry"));
    this.personRegistrySources = toLangValueMap(node.getProperties("personRegistrySources"));
    this.personRegisterDataTransfers = toLangValueMap(node.getProperties("personRegisterDataTransfers"));
    this.personRegisterDataTransfersOutsideEuOrEea = toLangValueMap(node.getProperties("personRegisterDataTransfersOutsideEuOrEea"));
    toOptionalString(node.getProperties("confidentialityClass"))
      .ifPresent(cc -> this.confidentialityClass = ConfidentialityClass.valueOf(cc));
    this.groundsForConfidentiality = toLangValueMap(node.getProperties("groundsForConfidentiality"));
    toOptionalString(node.getProperties("securityClassification"))
      .ifPresent(sc -> this.securityClassification = SecurityClassification.valueOf(sc));
    this.principlesForPhysicalSecurity = valuesToEnumCollection(node.getProperties("principlesForPhysicalSecurity"),
      PrincipleForPhysicalSecurity.class, ArrayList::new);
    this.principlesForDigitalSecurity = valuesToEnumCollection(node.getProperties("principlesForDigitalSecurity"),
      PrincipleForDigitalSecurity.class, ArrayList::new);
    this.comment = PropertyMappings.toString(node.getProperties("comment"));
    this.dataProcessingStartDate = toLocalDate(node.getProperties("dataProcessingStartDate"), null);
    this.dataProcessingEndDate = toLocalDate(node.getProperties("dataProcessingEndDate"), null);
    toOptionalString(node.getProperties("retentionPolicy"))
      .ifPresent(rp -> this.retentionPolicy = RetentionPolicy.valueOf(rp));
    this.retentionPeriod = toLangValueMap(node.getProperties("retentionPeriod"));
    this.groundsForRetention = toLangValueMap(node.getProperties("groundsForRetention"));
    this.nationalArchivesFinlandArchivalDecision = toLangValueMap(node.getProperties("nationalArchivesFinlandArchivalDecision"));
    this.existenceForms = valuesToEnumCollection(node.getProperties("existenceForms"),
      ExistenceForm.class, ArrayList::new);
    this.physicalLocation = toLangValueMap(node.getProperties("physicalLocation"));
    this.externalId = PropertyMappings.toString(node.getProperties("externalId"));

    node.getReferencesFirst("lastModifiedByUser")
      .ifPresent(v -> this.lastModifiedByUser = new UserInformation(new UserProfile(v)));
    node.getReferencesFirst("ownerOrganization")
      .ifPresent(oo -> this.ownerOrganization = new Organization(oo));
    node.getReferencesFirst("ownerOrganizationUnit")
      .ifPresent(oou -> this.ownerOrganizationUnit = new OrganizationUnit(oou));
    node.getReferences("personInRoles")
      .forEach(pir -> this.personInRoles.add(new PersonInRole(pir)));
    node.getReferences("links")
      .forEach(l -> this.links.add(new Link(l)));
    node.getReferencesFirst("usageCondition")
      .ifPresent(uc -> this.usageCondition = new UsageCondition(uc));
    node.getReferencesFirst("universe")
      .ifPresent(u -> this.universe = new Universe(u));
    node.getReferencesFirst("population")
      .ifPresent(p -> this.population = new Population(p));
    node.getReferencesFirst("unitType")
      .ifPresent(ut -> this.unitType = new UnitType(ut));
    node.getReferencesFirst("lifecyclePhase")
      .ifPresent(lp -> this.lifecyclePhase = new LifecyclePhase(lp));
    node.getReferences("conceptsFromScheme")
      .forEach(c -> this.conceptsFromScheme.add(new Concept(c)));
    node.getReferences("datasetTypes")
      .forEach(dt -> this.datasetTypes.add(new DatasetType(dt)));
    node.getReferencesFirst("studyGroup")
      .ifPresent(sg -> this.studyGroup = new StudyGroup(sg));
    node.getReferences("dataSets")
      .forEach(d -> this.datasets.add(new Dataset(d)));
    node.getReferences("predecessors")
      .forEach(s -> this.predecessors.add(new Study(s)));
    node.getReferences("systemInRoles")
      .forEach(sir -> this.systemInRoles.add(new SystemInRole(sir)));


    node.getReferrers("predecessors")
      .forEach(s -> this.successors.add(new Study(s)));
  }

  /**
   * Constructor for testing purposes. Add attributes if needed.
   */
  public Study(UUID id,
               Date lastModifiedDate,
               Map<String, String> prefLabel,
               Map<String, String> registryPolicy,
               Map<String, String> purposeOfPersonRegistry,
               Map<String, String> personRegistrySources,
               Map<String, String> personRegisterDataTransfers,
               Map<String, String> personRegisterDataTransfersOutsideEuOrEea,
               List<PrincipleForPhysicalSecurity> principlesForPhysicalSecurity,
               List<PrincipleForDigitalSecurity> principlesForDigitalSecurity,
               Organization ownerOrganization,
               List<PersonInRole> personInRoles,
               List<Dataset> datasets,
               List<Study> predecessors) {
    this.id = id;
    this.lastModifiedDate = lastModifiedDate;
    this.prefLabel = prefLabel;
    this.registryPolicy = registryPolicy;
    this.purposeOfPersonRegistry = purposeOfPersonRegistry;
    this.personRegistrySources = personRegistrySources;
    this.personRegisterDataTransfers = personRegisterDataTransfers;
    this.personRegisterDataTransfersOutsideEuOrEea = personRegisterDataTransfersOutsideEuOrEea;
    this.principlesForPhysicalSecurity = principlesForPhysicalSecurity;
    this.principlesForDigitalSecurity = principlesForDigitalSecurity;
    this.ownerOrganization = ownerOrganization;
    this.personInRoles = personInRoles;
    this.datasets = datasets;
    this.predecessors = predecessors;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  // TODO: Add date format? Currently serialized date looks like this: "lastModifiedDate": "Nov 1, 2017 4:26:32 PM"
  // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
  // And http://www.ecma-international.org/ecma-262/5.1/#sec-15.9.1.15
  public Optional<Date> getLastModifiedDate() {
    return Optional.ofNullable(lastModifiedDate);
  }

  public Optional<Boolean> isPublished() {
    return Optional.ofNullable(published);
  }

  public void setPublished(Boolean published) {
    this.published = published;
  }

  public Map<String, String> getPrefLabel() {
    return prefLabel;
  }

  public Map<String, String> getAltLabel() {
    return altLabel;
  }

  public Map<String, String> getAbbreviation() {
    return abbreviation;
  }

  public Map<String, String> getDescription() {
    return description;
  }

  public Map<String, String> getUsageConditionAdditionalInformation() {
    return usageConditionAdditionalInformation;
  }

  public Optional<LocalDate> getReferencePeriodStart() {
    return Optional.ofNullable(referencePeriodStart);
  }

  public Optional<LocalDate> getReferencePeriodEnd() {
    return Optional.ofNullable(referencePeriodEnd);
  }

  public Optional<LocalDate> getCollectionStartDate() {
    return Optional.ofNullable(collectionStartDate);
  }

  public Optional<LocalDate> getCollectionEndDate() {
    return Optional.ofNullable(collectionEndDate);
  }

  public Optional<String> getNumberOfObservationUnits() {
    return Optional.ofNullable(numberOfObservationUnits);
  }

  public Map<String, String> getFreeConcepts() {
    return freeConcepts;
  }

  public Optional<Boolean> getPersonRegistry() {
    return Optional.ofNullable(personRegistry);
  }

  public Map<String, String> getRegistryPolicy() {
    return registryPolicy;
  }

  public void setRegistryPolicy(Map<String, String> registryPolicy) {
    this.registryPolicy = registryPolicy;
  }

  public Map<String, String> getPurposeOfPersonRegistry() {
    return purposeOfPersonRegistry;
  }

  public void setPurposeOfPersonRegistry(Map<String, String> purposeOfPersonRegistry) {
    this.purposeOfPersonRegistry = purposeOfPersonRegistry;
  }

  public Map<String, String> getPersonRegistrySources() {
    return personRegistrySources;
  }

  public void setPersonRegistrySources(Map<String, String> personRegistrySources) {
    this.personRegistrySources = personRegistrySources;
  }

  public Map<String, String> getPersonRegisterDataTransfers() {
    return personRegisterDataTransfers;
  }

  public void setPersonRegisterDataTransfers(Map<String, String> personRegisterDataTransfers) {
    this.personRegisterDataTransfers = personRegisterDataTransfers;
  }

  public Map<String, String> getPersonRegisterDataTransfersOutsideEuOrEta() {
    return personRegisterDataTransfersOutsideEuOrEea;
  }

  public void setPersonRegisterDataTransfersOutsideEuOrEta(Map<String, String> personRegisterDataTransfersOutsideEuOrEea) {
    this.personRegisterDataTransfersOutsideEuOrEea = personRegisterDataTransfersOutsideEuOrEea;
  }

  public Optional<ConfidentialityClass> getConfidentialityClass() {
    return Optional.ofNullable(confidentialityClass);
  }

  public void setConfidentialityClass(ConfidentialityClass confidentialityClass) {
    this.confidentialityClass = confidentialityClass;
  }
  public Map<String, String> getGroundsForConfidentiality() {
    return groundsForConfidentiality;
  }

  public void setGroundsForConfidentiality(Map<String, String> groundsForConfidentiality) {
    this.groundsForConfidentiality = groundsForConfidentiality;
  }

  public Optional<SecurityClassification> getSecurityClassification() {
    return Optional.ofNullable(securityClassification);
  }

  public void setSecurityClassification(SecurityClassification securityClassification) {
    this.securityClassification = securityClassification;
  }

  public List<PrincipleForPhysicalSecurity> getPrinciplesForPhysicalSecurity() {
    return principlesForPhysicalSecurity;
  }

  public void setPrinciplesForPhysicalSecurity(List<PrincipleForPhysicalSecurity> principlesForPhysicalSecurity) {
    this.principlesForPhysicalSecurity = principlesForPhysicalSecurity;
  }

  public List<PrincipleForDigitalSecurity> getPrinciplesForDigitalSecurity() {
    return principlesForDigitalSecurity;
  }

  public void setPrinciplesForDigitalSecurity(List<PrincipleForDigitalSecurity> principlesForDigitalSecurity) {
    this.principlesForDigitalSecurity = principlesForDigitalSecurity;
  }

  public Optional<LocalDate> getDataProcessingStartDate() {
    return Optional.ofNullable(dataProcessingStartDate);
  }

  public Optional<LocalDate> getDataProcessingEndDate() {
    return Optional.ofNullable(dataProcessingEndDate);
  }

  public Map<String, String> getRetentionPeriod() {
    return retentionPeriod;
  }

  public Optional<RetentionPolicy> getRetentionPolicy() {
    return Optional.ofNullable(retentionPolicy);
  }

  public void setRetentionPolicy(RetentionPolicy retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

  public void setRetentionPeriod(Map<String, String> retentionPeriod) {
    this.retentionPeriod = retentionPeriod;
  }

  public Map<String, String> getGroundsForRetention() {
    return groundsForRetention;
  }

  public void setGroundsForRetention(Map<String, String> groundsForRetention) {
    this.groundsForRetention = groundsForRetention;
  }

  public Map<String, String> getNationalArchivesFinlandArchivalDecision() {
    return nationalArchivesFinlandArchivalDecision;
  }

  public Map<String, String> getPhysicalLocation() {
    return physicalLocation;
  }

  public List<ExistenceForm> getExistenceForms() {
    return existenceForms;
  }

  public void setExistenceForms(List<ExistenceForm> existenceForms) {
    this.existenceForms = existenceForms;
  }

  public void setPhysicalLocation(Map<String, String> physicalLocation) {
    this.physicalLocation = physicalLocation;
  }

  public Optional<String> getComment() {
    return Optional.ofNullable(comment);
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Optional<String> getExternalId() {
    return Optional.ofNullable(externalId);
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public Optional<UserProfile> getLastModifiedByUser() {
    return Optional.ofNullable(lastModifiedByUser);
  }

  public void setLastModifiedByUser(UserProfile userProfile) {
    this.lastModifiedByUser = userProfile;
  }

  public Optional<Organization> getOwnerOrganization() {
    return Optional.ofNullable(ownerOrganization);
  }

  public Optional<OrganizationUnit> getOwnerOrganizationUnit() {
    return Optional.ofNullable(ownerOrganizationUnit);
  }

  public List<PersonInRole> getPersonInRoles() {
    return personInRoles;
  }

  public List<Link> getLinks() {
    return links;
  }

  public Optional<UsageCondition> getUsageCondition() {
    return Optional.ofNullable(usageCondition);
  }

  public Optional<Universe> getUniverse() {
    return Optional.ofNullable(universe);
  }

  public Optional<Population> getPopulation() {
    return Optional.ofNullable(population);
  }

  public Optional<UnitType> getUnitType() {
    return Optional.ofNullable(unitType);
  }

  public Optional<LifecyclePhase> getLifecyclePhase() {
    return Optional.ofNullable(lifecyclePhase);
  }

  public List<Concept> getConceptsFromScheme() {
    return conceptsFromScheme;
  }

  public List<DatasetType> getDatasetTypes() {
    return datasetTypes;
  }

  public Optional<StudyGroup> getStudyGroup() {
    return Optional.ofNullable(studyGroup);
  }

  public List<Dataset> getDatasets() {
    return datasets;
  }

  public void setDatasets(List<Dataset> datasets) {
    this.datasets = datasets;
  }

  public List<Study> getPredecessors() {
    return predecessors;
  }

  public void setPredecessors(List<Study> predecessors) {
    this.predecessors = predecessors;
  }

  public List<Study> getSuccessors() {
    return successors;
  }

  public List<SystemInRole> getSystemInRoles() {
    return systemInRoles;
  }

  public void setSystemInRoles(List<SystemInRole> systemInRoles) {
    this.systemInRoles = systemInRoles;
  }

  public void setOwnerOrganizationUnit(OrganizationUnit ownerOrganizationUnit) {
    this.ownerOrganizationUnit = ownerOrganizationUnit;
  }

  public Boolean isChangedAfterPublish() {
    return changedAfterPublish;
  }

  public void setChangedAfterPublish(Boolean changedAfterPublish) {
    this.changedAfterPublish = changedAfterPublish;
  }

  /**
   * Transforms dataset into node
   */
  public Node toNode() {
    Multimap<String, StrictLangValue> props = LinkedHashMultimap.create();
    isPublished().ifPresent(v -> props.put("published", toPropertyValue(v)));
    props.putAll("prefLabel", toPropertyValues(prefLabel));
    props.putAll("altLabel", toPropertyValues(altLabel));
    props.putAll("abbreviation", toPropertyValues(abbreviation));
    props.putAll("description", toPropertyValues(description));
    props.putAll("usageConditionAdditionalInformation", toPropertyValues(usageConditionAdditionalInformation));
    getReferencePeriodStart().ifPresent(v -> props.put("referencePeriodStart", toPropertyValue(v)));
    getReferencePeriodEnd().ifPresent(v -> props.put("referencePeriodEnd", toPropertyValue(v)));
    getCollectionStartDate().ifPresent(v -> props.put("collectionStartDate", toPropertyValue(v)));
    getCollectionEndDate().ifPresent(v -> props.put("collectionEndDate", toPropertyValue(v)));
    getNumberOfObservationUnits().ifPresent(v -> props.put("numberOfObservationUnits", toPropertyValue(v)));
    props.putAll("freeConcepts", toPropertyValues(freeConcepts));
    getPersonRegistry().ifPresent(v -> props.put("personRegistry", toPropertyValue(v)));
    props.putAll("registryPolicy", toPropertyValues(registryPolicy));
    props.putAll("purposeOfPersonRegistry", toPropertyValues(purposeOfPersonRegistry));
    props.putAll("personRegistrySources", toPropertyValues(personRegistrySources));
    props.putAll("personRegisterDataTransfers", toPropertyValues(personRegisterDataTransfers));
    props.putAll("personRegisterDataTransfersOutsideEuOrEea", toPropertyValues(personRegisterDataTransfersOutsideEuOrEea));
    getConfidentialityClass().ifPresent(cc -> props.put("confidentialityClass", toPropertyValue(cc.toString())));
    props.putAll("groundsForConfidentiality", toPropertyValues(groundsForConfidentiality));
    getSecurityClassification().ifPresent(sc -> props.put("securityClassification", toPropertyValue(sc.toString())));
    props.putAll("principlesForPhysicalSecurity", PropertyMappings.enumsToPropertyValues(principlesForPhysicalSecurity));
    props.putAll("principlesForDigitalSecurity", PropertyMappings.enumsToPropertyValues(principlesForDigitalSecurity));
    getDataProcessingStartDate().ifPresent(v -> props.put("dataProcessingStartDate", toPropertyValue(v)));
    getDataProcessingEndDate().ifPresent(v -> props.put("dataProcessingEndDate", toPropertyValue(v)));
    getRetentionPolicy().ifPresent(rp -> props.put("retentionPolicy", toPropertyValue(rp.toString())));
    props.putAll("retentionPeriod", toPropertyValues(retentionPeriod));
    props.putAll("groundsForRetention", toPropertyValues(groundsForRetention));
    props.putAll("nationalArchivesFinlandArchivalDecision", toPropertyValues(nationalArchivesFinlandArchivalDecision));
    props.putAll("existenceForms", PropertyMappings.enumsToPropertyValues(existenceForms));
    props.putAll("physicalLocation", toPropertyValues(physicalLocation));
    getComment().ifPresent(v -> props.put("comment", toPropertyValue(v)));
    getExternalId().ifPresent(ei -> props.put("externalId", toPropertyValue(ei)));

    Multimap<String, Node> refs = LinkedHashMultimap.create();
    getLastModifiedByUser().ifPresent(v -> refs.put("lastModifiedByUser", v.toNode()));
    getOwnerOrganization().ifPresent(oo -> refs.put("ownerOrganization", oo.toNode()));
    getOwnerOrganizationUnit().ifPresent(oou -> refs.put("ownerOrganizationUnit", oou.toNode()));
    getPersonInRoles().forEach(pir -> refs.put("personInRoles", pir.toNode()));
    getLinks().forEach(l -> refs.put("links", l.toNode()));
    getUsageCondition().ifPresent(uc -> refs.put("usageCondition", uc.toNode()));
    getUniverse().ifPresent(u -> refs.put("universe", u.toNode()));
    getPopulation().ifPresent(p -> refs.put("population", p.toNode()));
    getUnitType().ifPresent(ut -> refs.put("unitType", ut.toNode()));
    getLifecyclePhase().ifPresent(lp -> refs.put("lifecyclePhase", lp.toNode()));
    getConceptsFromScheme().forEach(c -> refs.put("conceptsFromScheme", c.toNode()));
    getDatasetTypes().forEach(dt -> refs.put("datasetTypes", dt.toNode()));
    getStudyGroup().ifPresent(sg -> refs.put("studyGroup", sg.toNode()));
    getDatasets().forEach(d -> refs.put("dataSets", d.toNode()));
    getPredecessors().forEach(d -> refs.put("predecessors", d.toNode()));
    getSystemInRoles().forEach(sir -> refs.put("systemInRoles", sir.toNode()));

    return new Node(id, TERMED_NODE_CLASS, props, refs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Study study = (Study) o;
    return Objects.equals(id, study.id)
            && Objects.equals(lastModifiedDate, study.lastModifiedDate)
            && Objects.equals(published, study.published)
            && Objects.equals(prefLabel, study.prefLabel)
            && Objects.equals(altLabel, study.altLabel)
            && Objects.equals(abbreviation, study.abbreviation)
            && Objects.equals(description, study.description)
            && Objects.equals(usageConditionAdditionalInformation, study.usageConditionAdditionalInformation)
            && Objects.equals(referencePeriodStart, study.referencePeriodStart)
            && Objects.equals(referencePeriodEnd, study.referencePeriodEnd)
            && Objects.equals(collectionStartDate, study.collectionStartDate)
            && Objects.equals(collectionEndDate, study.collectionEndDate)
            && Objects.equals(numberOfObservationUnits, numberOfObservationUnits)
            && Objects.equals(freeConcepts, study.freeConcepts)
            && Objects.equals(personRegistry, study.personRegistry)
            && Objects.equals(registryPolicy, study.registryPolicy)
            && Objects.equals(purposeOfPersonRegistry, study.purposeOfPersonRegistry)
            && Objects.equals(personRegistrySources, study.personRegistrySources)
            && Objects.equals(personRegisterDataTransfers, study.personRegisterDataTransfers)
            && Objects.equals(personRegisterDataTransfersOutsideEuOrEea, study.personRegisterDataTransfersOutsideEuOrEea)
            && Objects.equals(confidentialityClass, study.confidentialityClass)
            && Objects.equals(groundsForConfidentiality, study.groundsForConfidentiality)
            && Objects.equals(securityClassification, study.securityClassification)
            && Objects.equals(principlesForPhysicalSecurity, study.principlesForPhysicalSecurity)
            && Objects.equals(principlesForDigitalSecurity, study.principlesForDigitalSecurity)
            && Objects.equals(dataProcessingStartDate, study.dataProcessingStartDate)
            && Objects.equals(dataProcessingEndDate, study.dataProcessingEndDate)
            && Objects.equals(retentionPolicy, study.retentionPolicy)
            && Objects.equals(retentionPeriod, study.retentionPeriod)
            && Objects.equals(groundsForRetention, study.groundsForRetention)
            && Objects.equals(nationalArchivesFinlandArchivalDecision, study.nationalArchivesFinlandArchivalDecision)
            && Objects.equals(existenceForms, study.existenceForms)
            && Objects.equals(physicalLocation, study.physicalLocation)
            && Objects.equals(comment, study.comment)
            && Objects.equals(externalId, study.externalId)
            && Objects.equals(lastModifiedByUser, study.lastModifiedByUser)
            && Objects.equals(ownerOrganization, study.ownerOrganization)
            && Objects.equals(ownerOrganizationUnit, study.ownerOrganizationUnit)
            && Objects.equals(personInRoles, study.personInRoles)
            && Objects.equals(links, study.links)
            && Objects.equals(usageCondition, study.usageCondition)
            && Objects.equals(universe, study.universe)
            && Objects.equals(population, study.population)
            && Objects.equals(unitType, study.unitType)
            && Objects.equals(lifecyclePhase, study.lifecyclePhase)
            && Objects.equals(conceptsFromScheme, study.conceptsFromScheme)
            && Objects.equals(datasetTypes, study.datasetTypes)
            && Objects.equals(studyGroup, study.studyGroup)
            && Objects.equals(datasets, study.datasets)
            && Objects.equals(predecessors, study.predecessors)
            && Objects.equals(systemInRoles, study.systemInRoles);
  }

  @Override
  public int hashCode() {
      return Objects.hash(id,
        lastModifiedDate,
        published,
        prefLabel,
        altLabel,
        abbreviation,
        description,
        usageConditionAdditionalInformation,
        referencePeriodStart,
        referencePeriodEnd,
        collectionStartDate,
        collectionEndDate,
        numberOfObservationUnits,
        freeConcepts,
        personRegistry,
        registryPolicy,
        purposeOfPersonRegistry,
        personRegistrySources,
        personRegisterDataTransfers,
        personRegisterDataTransfersOutsideEuOrEea,
        confidentialityClass,
        groundsForConfidentiality,
        securityClassification,
        principlesForPhysicalSecurity,
        principlesForDigitalSecurity,
        dataProcessingStartDate,
        dataProcessingEndDate,
        retentionPolicy,
        existenceForms,
        retentionPeriod,
        groundsForRetention,
        nationalArchivesFinlandArchivalDecision,
        physicalLocation,
        comment,
        externalId,
        lastModifiedByUser,
        ownerOrganization,
        ownerOrganizationUnit,
        personInRoles,
        links,
        usageCondition,
        universe,
        population,
        unitType,
        lifecyclePhase,
        conceptsFromScheme,
        datasetTypes,
        studyGroup,
        datasets,
        predecessors,
        systemInRoles
      );
  }

  @JsonIgnore
  public Study getSimplified() {
    Study study = new Study();
    study.id = this.id;
    study.prefLabel = this.prefLabel;
    study.altLabel = this.altLabel;
    study.abbreviation = this.abbreviation;
    study.description = this.description;
    study.usageConditionAdditionalInformation = this.usageConditionAdditionalInformation;
    study.referencePeriodStart = this.referencePeriodStart;
    study.referencePeriodEnd = this.referencePeriodEnd;
    study.collectionStartDate = this.collectionStartDate;
    study.collectionEndDate = this.collectionEndDate;
    study.numberOfObservationUnits = this.numberOfObservationUnits;
    study.freeConcepts = this.freeConcepts;
    study.personRegistry = this.personRegistry;
    study.registryPolicy = this.registryPolicy;
    study.purposeOfPersonRegistry = this.purposeOfPersonRegistry;
    study.personRegistrySources = this.personRegistrySources;
    study.personRegisterDataTransfers = this.personRegisterDataTransfers;
    study.personRegisterDataTransfersOutsideEuOrEea = this.personRegisterDataTransfersOutsideEuOrEea;
    study.confidentialityClass = this.confidentialityClass;
    study.groundsForConfidentiality = this.groundsForConfidentiality;
    study.securityClassification = this.securityClassification;
    study.principlesForPhysicalSecurity = this.principlesForPhysicalSecurity;
    study.principlesForDigitalSecurity = this.principlesForDigitalSecurity;
    study.dataProcessingStartDate = this.dataProcessingStartDate;
    study.dataProcessingEndDate = this.dataProcessingEndDate;
    study.retentionPolicy = this.retentionPolicy;
    study.retentionPeriod = this.retentionPeriod;
    study.groundsForRetention = this.groundsForRetention;
    study.nationalArchivesFinlandArchivalDecision = this.nationalArchivesFinlandArchivalDecision;
    study.existenceForms = this.existenceForms;
    study.ownerOrganization = this.ownerOrganization;
    study.ownerOrganizationUnit = this.ownerOrganizationUnit;
    study.links = this.links;
    study.usageCondition = this.usageCondition;
    study.universe = this.universe;
    study.population = this.population;
    study.unitType = this.unitType;
    study.lifecyclePhase = this.lifecyclePhase;
    study.conceptsFromScheme = this.conceptsFromScheme;
    study.datasetTypes = this.datasetTypes;
    study.studyGroup = this.studyGroup;
    study.systemInRoles = this.systemInRoles;

    for (PersonInRole personInRole : this.personInRoles) {
      if (personInRole.isPublic().isPresent() && personInRole.isPublic().get().equals(Boolean.TRUE)) {
        study.personInRoles.add(personInRole);
      }
    }

    for (Study predecessor : this.predecessors) {
      if (predecessor.isPublished().isPresent() && predecessor.isPublished().get().equals(Boolean.TRUE)) {
        study.predecessors.add(predecessor.getSimplified());
      }
    }

    for (Dataset dataset : this.datasets) {
      study.datasets.add(dataset.getSimplified());
    }

    return study;
  }
}
