package com.tcl.aem.tclweb.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;

import javax.mail.internet.InternetAddress;

import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.Workflow;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;


@Component(service=WorkflowProcess.class, property = {"process.label=Approval Workflow Process"})

public class ApprovalWorkflow implements WorkflowProcess {
	private static Logger LOGGER = LoggerFactory.getLogger(ApprovalWorkflow.class);
	@Reference
	ResourceResolverFactory resolverFactory;
	@Reference 
	private MessageGatewayService messageService;
	public void execute(WorkItem item, WorkflowSession wfsession, MetaDataMap mData)
			throws WorkflowException {
		WorkflowData workflowData = item.getWorkflowData();
		String damPath=workflowData.getPayload().toString();
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
			String approvedImagePath="/content/dam/tclproject/approved-images/"+assetName;
			String approvedPDFPath="/content/dam/tclproject/approved-pdf/"+assetName;
			AssetManager assestManager=resolver.adaptTo(AssetManager.class);
			if(damPath.contains("raw-images")){
				assestManager.moveAsset(damPath, approvedImagePath);
			addMetadata(approvedImagePath,resolver);}
			else{
				assestManager.moveAsset(damPath, approvedPDFPath);
				addMetadata(approvedPDFPath,resolver);
			}
			systemUserSession.save();
			String userEmail = PropertiesUtil.toString(authorizable.getProperty("profile/email"), "");
			if(StringUtils.isBlank(userEmail)) {
	            return;
	        }
			
			LOGGER.info("###userEmail####"+userEmail);
            emailRecipients.add(new InternetAddress(userEmail));
            HtmlEmail email = new HtmlEmail();
            email.setCharset(CharEncoding.UTF_8);
            email.setTo(emailRecipients);
            email.setSubject("Approved Asset");
            email.setMsg("text email body");
            email.setHtmlMsg("<!DOCTYPE html><html><head></head><body><p>Asset has been approved by approver</p></body></html>");
            messageGateway.send(email);
        
	} catch (Exception e) {
		LOGGER.error("messageerror" + e.getMessage());
	}
	
	}

	private void addMetadata(String approvedPath, ResourceResolver resolver) {
		LOGGER.info("INSIDE METHOD"+approvedPath);
		try {
		Resource approvedRessource=resolver.getResource(approvedPath+"/jcr:content");
		Node node = approvedRessource.adaptTo(Node.class);
		node.setProperty("approvedStatus", "Approved");
		LOGGER.info("messageError" + node.getName());
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		}

	
}
