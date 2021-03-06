package org.ftn.upp.lass.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.ftn.upp.lass.common.LogMessages;
import org.ftn.upp.lass.common.api.RestApiEndpoints;
import org.ftn.upp.lass.common.api.RestApiRequestParameters;
import org.ftn.upp.lass.dto.response.ProcessInfoResponse;
import org.ftn.upp.lass.exception.BadRequestException;
import org.ftn.upp.lass.exception.BadRequestResponseCode;
import org.ftn.upp.lass.util.ErrorMessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping(RestApiEndpoints.PROCESS_MANAGEMENT)
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessManagementController {

    private final RuntimeService runtimeService;

    @GetMapping(RestApiEndpoints.START_PROCESS_INSTANCE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ProcessInfoResponse startProcessInstance(@RequestParam(RestApiRequestParameters.PROCESS_NAME) @NotBlank String processName) {
        log.info(LogMessages.STARTING_PROCESS, processName);

        final ProcessInstance processInstance;
        try {
            processInstance = this.runtimeService.startProcessInstanceByKey(processName);
        } catch (ProcessEngineException e) {
            throw new BadRequestException(BadRequestResponseCode.INVALID_REQUEST_DATA, ErrorMessageUtil.processWithGivenNameNotFound(processName));
        }

        log.info(LogMessages.PROCESS_STARTED, processInstance.getId(), processName);
        return ProcessInfoResponse.builder()
                .processInstanceId(processInstance.getId())
                .build();
    }
}
