package org.ftn.upp.literaryassociation.repository;

import org.ftn.upp.literaryassociation.model.PublishedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishedBookRepository extends JpaRepository<PublishedBook, Long>, JpaSpecificationExecutor<PublishedBook> {

}
