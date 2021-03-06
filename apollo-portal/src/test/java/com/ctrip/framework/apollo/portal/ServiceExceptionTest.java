package com.ctrip.framework.apollo.portal;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.framework.apollo.common.entity.App;
import com.ctrip.framework.apollo.common.exception.ServiceException;
import com.ctrip.framework.apollo.portal.controller.AppController;
import com.ctrip.framework.apollo.portal.spi.UserService;

import com.google.gson.Gson;

public class ServiceExceptionTest extends AbstractUnitTest {

	@InjectMocks
	private AppController appController;
	@Mock
	private UserService userService;


	@Test
	public void testAdminServiceException() {
		String errorMsg = "No available admin service";
		String errorCode = "errorCode";
		String status = "500";

		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("status", status);
		errorAttributes.put("message", errorMsg);
		errorAttributes.put("timestamp",
												LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		errorAttributes.put("exception", ServiceException.class.getName());
		errorAttributes.put("errorCode", errorCode);

		HttpStatusCodeException adminException =
				new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "admin server error",
																		 new Gson().toJson(errorAttributes).getBytes(), Charset.defaultCharset());

		when(userService.findByUserId(any(String.class))).thenThrow(adminException);

		App app = generateSampleApp();
		try {
			appController.create(app);
		} catch (HttpStatusCodeException e) {
			@SuppressWarnings("unchecked")
			Map<String, String> attr = new Gson().fromJson(e.getResponseBodyAsString(), Map.class);
			Assert.assertEquals(errorMsg, attr.get("message"));
			Assert.assertEquals(errorCode, attr.get("errorCode"));
			Assert.assertEquals(status, attr.get("status"));
		}
	}

	private App generateSampleApp() {
		App app = new App();
		app.setAppId("someAppId");
		app.setName("someName");
		app.setOrgId("someOrgId");
		app.setOrgName("someOrgNam");
		app.setOwnerName("someOwner");
		app.setOwnerEmail("someOwner@ctrip.com");
		return app;
	}
}
