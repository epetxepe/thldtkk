package fi.thl.thldtkk.api.metadata.service;

import fi.thl.thldtkk.api.metadata.domain.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationService extends Service<UUID, Organization> {
  Optional<Organization> getByVirtuId(String virtuId);
}
