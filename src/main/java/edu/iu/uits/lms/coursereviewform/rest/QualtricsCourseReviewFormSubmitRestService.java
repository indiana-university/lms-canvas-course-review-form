package edu.iu.uits.lms.coursereviewform.rest;

/*-
 * #%L
 * course-review-form
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.coursereviewform.model.QualtricsCourse;
import edu.iu.uits.lms.coursereviewform.model.QualtricsDocument;
import edu.iu.uits.lms.coursereviewform.model.QualtricsRestSubmission;
import edu.iu.uits.lms.coursereviewform.model.QualtricsSubmission;
import edu.iu.uits.lms.coursereviewform.service.QualtricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping({"/rest/submit"})
@Slf4j
public class QualtricsCourseReviewFormSubmitRestService {
    @Autowired
    private QualtricsService qualtricsService;

    @PostMapping("/fromqualtrics/{document_id}")
    public void submit(@PathVariable("document_id") Long documentId, @RequestHeader Map<String, String> headers, @RequestBody QualtricsRestSubmission qualtricsRestSubmission) {
        if (qualtricsRestSubmission == null || qualtricsRestSubmission.getCourseId() == null ||
                qualtricsRestSubmission.getLastSubmittedBy() == null ||
                qualtricsRestSubmission.getResponseId() == null ||
                documentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing document information");
        }

        String tokenHeader = headers.get("x-api-token");

        if (tokenHeader == null) {
            log.error("Auth header not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auth header not found");
        }

        QualtricsDocument qualtricsDocument = qualtricsService.getDocument(documentId);

        if (qualtricsDocument == null) {
            log.error("Could not find document by id = {}", documentId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing job information - id");
        }

        if (! tokenHeader.equals(qualtricsDocument.getToken())) {
            log.error("The provided token {} doesn't match the expected token value {}", tokenHeader, qualtricsDocument.getToken());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided token doesn't match expected token value");
        }

        final String courseId = qualtricsRestSubmission.getCourseId();

        QualtricsCourse qualtricsCourse = qualtricsService.getCourse(qualtricsDocument, courseId);

        if (qualtricsCourse == null) {
            log.error("Cannot find course with id {} for document id {}", courseId, qualtricsDocument.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot find course for document");
        }

        QualtricsSubmission qualtricsSubmission = new QualtricsSubmission();
        qualtricsSubmission.setQualtricsCourse(qualtricsCourse);
        qualtricsSubmission.setUserId(qualtricsRestSubmission.getLastSubmittedBy());
        qualtricsSubmission.setResponseId(qualtricsRestSubmission.getResponseId());

        qualtricsSubmission = qualtricsService.saveSubmission(qualtricsSubmission);

        qualtricsCourse.setOpen(false);
        qualtricsCourse = qualtricsService.saveCourse(qualtricsCourse);

        log.info("Saved submission id {} from user {} for documentId {}",
                qualtricsSubmission.getId(), qualtricsSubmission.getUserId(), qualtricsDocument.getId());
    }
}
