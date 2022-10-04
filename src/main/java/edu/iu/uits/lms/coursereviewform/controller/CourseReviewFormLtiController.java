//package edu.iu.uits.lms.coursereviewform.controller;

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
//
//import edu.iu.uits.lms.common.session.CourseSessionService;
//import edu.iu.uits.lms.lti.controller.LtiController;
//import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
//import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
//import io.jsonwebtoken.Claims;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.tsugi.basiclti.BasicLTIConstants;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//@RequestMapping({"/lti"})
//@Slf4j
//public class CourseReviewFormLtiController extends LtiController {
//    @Autowired
//    private CourseSessionService courseSessionService;
//
//    private boolean openLaunchUrlInNewWindow = false;
//
//    public static final String CUSTOM_DOCUMENT_ID = "custom_document_id";
//
//    @Override
//    protected String getLaunchUrl(Map<String, String> launchParams) {
//        String courseId = launchParams.get(CUSTOM_CANVAS_COURSE_ID);
//        String documentId = launchParams.get(CUSTOM_DOCUMENT_ID);
//
//        return "/app/index/" + courseId + "/" + documentId;
//    }
//
//    @Override
//    protected Map<String, String> getParametersForLaunch(Map<String, String> payload, Claims claims) {
//        Map<String, String> paramMap = new HashMap<String, String>(1);
//
//        paramMap.put(CUSTOM_CANVAS_COURSE_ID, payload.get(CUSTOM_CANVAS_COURSE_ID));
//        paramMap.put(BasicLTIConstants.ROLES, payload.get(BasicLTIConstants.ROLES));
//        paramMap.put(CUSTOM_CANVAS_USER_LOGIN_ID, payload.get(CUSTOM_CANVAS_USER_LOGIN_ID));
//        paramMap.put(BasicLTIConstants.CONTEXT_TITLE, payload.get(BasicLTIConstants.CONTEXT_TITLE));
//        paramMap.put(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, payload.get(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY));
//        paramMap.put(BasicLTIConstants.LIS_PERSON_SOURCEDID, payload.get(BasicLTIConstants.LIS_PERSON_SOURCEDID));
//        paramMap.put(BasicLTIConstants.LIS_PERSON_NAME_FULL, payload.get(BasicLTIConstants.LIS_PERSON_NAME_FULL));
//
//        paramMap.put(CUSTOM_DOCUMENT_ID, payload.get(CUSTOM_DOCUMENT_ID));
//
//        openLaunchUrlInNewWindow = Boolean.valueOf(payload.get(CUSTOM_OPEN_IN_NEW_WINDOW));
//
//        return paramMap;
//    }
//
//    @Override
//    protected void preLaunchSetup(Map<String, String> launchParams, HttpServletRequest request, HttpServletResponse response) {
//        String rolesString = launchParams.get(BasicLTIConstants.ROLES);
//        String[] userRoles = rolesString.split(",");
//        String authority = returnEquivalentAuthority(Arrays.asList(userRoles), getDefaultInstructorRoles());
//        log.debug("LTI equivalent authority: " + authority);
//
//        String userId = launchParams.get(CUSTOM_CANVAS_USER_LOGIN_ID);
//        String userFullName = launchParams.get(BasicLTIConstants.LIS_PERSON_NAME_FULL);
//        String systemId = launchParams.get(BasicLTIConstants.TOOL_CONSUMER_INSTANCE_GUID);
//        String courseId = launchParams.get(CUSTOM_CANVAS_COURSE_ID);
//        String courseTitle = launchParams.get(BasicLTIConstants.CONTEXT_TITLE);
//
//        HttpSession session = request.getSession();
//
//        courseSessionService.addAttributeToSession(session, courseId, BasicLTIConstants.LIS_PERSON_NAME_FULL, userFullName);
//        courseSessionService.addAttributeToSession(session, courseId, BasicLTIConstants.CONTEXT_TITLE, courseTitle);
//
//        LtiAuthenticationToken token = new LtiAuthenticationToken(userId,
//                courseId, systemId, AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, authority), getToolContext());
//        SecurityContextHolder.getContext().setAuthentication(token);
//    }
//
//    @Override
//    protected String getToolContext() {
//        return "course-review-form";
//    }
//
//    @Override
//    protected LAUNCH_MODE launchMode() {
//        if (openLaunchUrlInNewWindow)
//            return LAUNCH_MODE.WINDOW;
//
//        return LAUNCH_MODE.FORWARD;
//    }
//}
