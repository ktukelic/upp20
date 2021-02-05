package org.ftn.upp.lass.integration.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.ftn.upp.lass.common.Constants;
import org.ftn.upp.lass.common.LogMessages;
import org.ftn.upp.lass.model.MembershipStatus;
import org.ftn.upp.lass.repository.AuthorRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SetAuthorMembershipStatusToNotPaid implements JavaDelegate {

    private final AuthorRepository authorRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info(LogMessages.EXECUTE, SetAuthorMembershipStatusToNotPaid.class.getName());

        final var username = (String) execution.getVariable(Constants.FormFieldVariables.USERNAME);
        final var authorOptional = this.authorRepository.findAuthorByUsername(username);
        if (authorOptional.isPresent()) {
            final var author = authorOptional.get();
            author.setMembershipStatus(MembershipStatus.NOT_PAID);

            this.authorRepository.save(author);
        }

        log.info(LogMessages.FINISHED, SetAuthorMembershipStatusToNotPaid.class.getName());
    }
}