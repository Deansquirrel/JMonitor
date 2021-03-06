package com.yuansong.controller;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.yuansong.common.CommonFun;
import com.yuansong.pojo.WebStateTaskConfig;
import com.yuansong.service.ConfigService;
import com.yuansong.service.MessageSenderManagerService;
import com.yuansong.service.TaskWorkerManagerService;
import com.yuansong.taskjob.TaskWorkerWebState;

@Controller
@RequestMapping(value="/TaskConfig/WebState")
public class TaskConfigWebStateController {
	
	private final Logger logger = Logger.getLogger(TaskConfigWebStateController.class);

	private Gson mGson = new Gson();
	
	@Autowired
	private ConfigService<WebStateTaskConfig> webStateTaskConfigService;
	
	@Autowired
	private TaskWorkerManagerService taskWorkerManagerService;
	
	@Autowired
	private MessageSenderManagerService messageSenderManagerService;
	
	@RequestMapping(value="/List")
	public ModelAndView webStateTaskConfigList(Map<String, Object> model) {
		logger.debug("RootController webStateTaskConfigList");
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> listItem;
		for(WebStateTaskConfig config : webStateTaskConfigService.getSetConfigList()) {
			listItem = new HashMap<String, String>();
			listItem.put("id", config.getId());
			listItem.put("title", config.getTitle());
			listItem.put("remark", config.getRemark());
			list.add(listItem);
		}
		list.sort(new Comparator<Map<String, String>>(){
			@Override
			public int compare(Map<String, String> o1, Map<String, String> o2) {
				String str1 = o1.get("title") + o1.get("remark");
				String str2 = o2.get("title") + o2.get("remark");
				Collator instance = Collator.getInstance(Locale.CHINA);
				return instance.compare(str1, str2);
			}
		});
		model.put("list", list);
		
		List<String> menuList = new ArrayList<String>();
		menuList.add("TaskConfig");
		menuList.add("WebState");
		menuList.add("List");
		
		model.put("menulist", mGson.toJson(menuList));
		
		return new ModelAndView("TaskConfigWebStateList", model);
	}
	
	
	@RequestMapping(value="/Add",method=RequestMethod.GET)
	public ModelAndView webStateTaskConfigAdd(Map<String, Object> model) {
		logger.debug("RootController webStateTaskConfigAdd");
		
		List<String> menuList = new ArrayList<String>();
		menuList.add("TaskConfig");
		menuList.add("WebState");
		menuList.add("Add");
		
		model.put("menulist", mGson.toJson(menuList));
		
		return new ModelAndView("TaskConfigWebStateAdd", model);
	}
	
	@Transactional
	@RequestMapping(value="/Add",method=RequestMethod.POST)
	public ModelAndView webStateTaskConfigAddAction(
			@RequestParam("i-title") String title,
			@RequestParam("i-remark") String remark,
			@RequestParam("i-url") String url,
			@RequestParam("i-cron") String cron,
			@RequestParam("i-msgtitle") String msgTitle,
			@RequestParam("i-msgcontent") String msgContent,
			Map<String, Object> model) {
		logger.debug("RootController webStateTaskConfigAddAction");
		
		WebStateTaskConfig config = new WebStateTaskConfig();
		config.setId(CommonFun.UUID());
		config.setTitle(title.trim());
		config.setRemark(remark.trim());
		config.setUrl(url);
		config.setCron(cron);
		config.setMsgTitle(msgTitle);
		config.setMsgContent(msgContent);
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("errCode", "0");
		data.put("errDesc","success");
		
		try {
			taskWorkerManagerService.addTask(config.getId(),  new TaskWorkerWebState(config, messageSenderManagerService.getMessageSenderList()), config.getCron());
			taskWorkerManagerService.cancelTask(config.getId());
			String check = webStateTaskConfigService.checkConfig(config);
			if(check.equals("")) {
				webStateTaskConfigService.add(config);
			}
			else {
				data.put("errCode", "1");
				data.put("errDesc",check);
			}
		}catch(Exception ex) {
			data.put("errCode", "-1");
			data.put("errDesc",ex.getMessage());
			ex.printStackTrace();
		}
		
		model.put("info", mGson.toJson(data));
		
		return new ModelAndView("responsePage", model);
	}
	
	@Transactional
	@RequestMapping(value="/Del",method=RequestMethod.POST)
	public ModelAndView webStateTaskConfigDelAction(
			@RequestParam("i-id") String id,
			Map<String, Object> model) {
		logger.debug("RootController webStateTaskConfigDelAction");
		logger.debug(id);
		
		WebStateTaskConfig config = new WebStateTaskConfig();
		config.setId(id);
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("errCode", "0");
		data.put("errDesc","success");
		
		try {
			webStateTaskConfigService.del(config);
		}catch(Exception ex) {
			data.put("errCode", "-1");
			data.put("errDesc",ex.getMessage());
			ex.printStackTrace();
		}
		
		model.put("info", mGson.toJson(data));
		
		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/Detail/{taskId}")
	public ModelAndView webStateTaskConfigDetail(@PathVariable String taskId, Map<String, Object> model) {
		logger.debug("RootController webStateTaskConfigDetail - " + taskId);
		
		if(!taskId.trim().equals("")) {
			WebStateTaskConfig config = webStateTaskConfigService.getSetConfig(taskId);
			if(config != null) {
				model.put("config",config);
			}
		}
		
		List<String> menuList = new ArrayList<String>();
		menuList.add("TaskConfig");
		menuList.add("WebState");
		menuList.add("List");
		menuList.add("Detail");
		
		model.put("menulist", mGson.toJson(menuList));
		
		return new ModelAndView("TaskConfigWebStateDetail", model);
	}
	
}
