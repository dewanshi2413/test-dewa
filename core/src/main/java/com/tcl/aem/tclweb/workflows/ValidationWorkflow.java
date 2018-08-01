package com.tcl.aem.tclweb.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.InternetAddress;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.Route;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;


@Component(service=WorkflowProcess.class, property = {"process.label=Validation Workflow Process"})

public class ValidationWorkflow implements WorkflowProcess {
	private static Logger LOGGER = LoggerFactory.getLogger(ValidationWorkflow.class);
	@Reference
	ResourceResolverFactory resolverFactory;
	
	@Reference 
	private MessageGatewayService messageService;

	public void execute(WorkItem item, WorkflowSession wfsession, MetaDataMap mData)
			throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
		String damPath=workflowData.getPayload().toString();
		LOGGER.info("%%%%DAMPATH%%%"+damPath);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(ResourceResolverFactory.SUBSERVICE, "getTCLinfo");
		ResourceResolver resolver = null;
		Session systemUserSession;
		try {
			resolver = resolverFactory.getServiceResourceResolver(param);
			systemUserSession = resolver.adaptTo(Session.class);
			ArrayList<InternetAddress> emailRecipients = new ArrayList<InternetAddress>();
			MessageGateway<HtmlEmail> messageGateway = messageService.getGateway(HtmlEmail.class);
			UserManager manager = resolver.adaptTo(UserManager.class);
	        Authorizable authorizable = manager.getAuthorizable(item.getWorkflow().getInitiator());
	        
			Node root = systemUserSession.getNode(damPath);
			String assetName=root.getName();
			//String assetName=root.getParent().getName();
			LOGGER.info("$$$$assetName$$$$$"+assetName);
			
			boolean validAssetName=checkValidation(assetName);
			systemUserSession.save();
			LOGGER.info("$$$$validAssetName$$$$$"+validAssetName);
			if(validAssetName){
				List<Route> routes = wfsession.getRoutes(item);
				Route route=routes.get(0);
				LOGGER.info("$$$$validAssetName$$$$$"+routes.get(0).getName());
				wfsession.complete(item,route);
				String userEmail = PropertiesUtil.toString(authorizable.getProperty("profile/email"), "");
		        if(StringUtils.isBlank(userEmail)) {
		            return;
		        }
				LOGGER.info("###userEmail####"+userEmail);
		        emailRecipients.add(new InternetAddress(userEmail));
		        HtmlEmail email = new HtmlEmail();
		        email.setCharset(CharEncoding.UTF_8);
		        email.setTo(emailRecipients);
		        email.setSubject("Valid Asset Name");
		        email.setMsg("text email body");
		        email.setHtmlMsg("<!DOCTYPE html><html><head></head><body><p>Your Asset has been validated and sent to Approver</p></body></html>");
		        messageGateway.send(email);
		        LOGGER.info("$$$$TEST EMAIL$$$$$");
				 
			}
			else{
				LOGGER.info("$$$$INSIDE ELSE$$$$$");
				Workflow workflow = wfsession.getWorkflow(item.getWorkflow().getId());
				wfsession.terminateWorkflow(workflow);
				String userEmail = PropertiesUtil.toString(authorizable.getProperty("profile/email"), "");
				LOGGER.info("###userEmail####"+userEmail);
		        if(StringUtils.isBlank(userEmail)) {
		            return;
		        }
				
				LOGGER.info("###userEmail####"+userEmail);
	            emailRecipients.add(new InternetAddress(userEmail));
	            HtmlEmail email = new HtmlEmail();
	            email.setCharset(CharEncoding.UTF_8);
	            email.setTo(emailRecipients);
	            email.setSubject("Invalid Asset Name");
	            email.setMsg("text email body");
	            email.setHtmlMsg("<!DOCTYPE html><html><head></head><body><p>Please remove special characters & whitespaces from "+assetName+"</p></body></html>");
	            messageGateway.send(email);
	        
			}
			
		} catch (Exception e) {
			LOGGER.error("messageerror" + e.getMessage());
		}
	        }

	

	//Check Validation method to validate asset name
	private boolean checkValidation(String assetName) {
		LOGGER.info("$$$$INSIDE CHECK VALIDATION$$$$$");
		boolean flag = false;
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile("[\\s$#%&@!_ ]", Pattern.CASE_INSENSITIVE);
	      matcher = pattern.matcher(assetName);
	      if(matcher.find())
				flag=false;
	      else
	    	  	flag=true;
	     return flag;
		
	}
		
	}
	

  
