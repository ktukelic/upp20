package org.ftn.upp.lass.repository;

import org.ftn.upp.lass.model.PrintingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PrintingRequestRepository extends JpaRepository<PrintingRequest, Long>, JpaSpecificationExecutor<PrintingRequest> {

}
