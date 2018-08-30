package com.yuansong.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.yuansong.common.CommonFun;
import com.yuansong.common.HttpUtils;
import com.yuansong.notify.DingMessageSender;
import com.yuansong.pojo.HealthTaskConfig;
import com.yuansong.pojo.IntTaskConfig;
import com.yuansong.pojo.WebStateTaskConfig;
import com.yuansong.service.ConfigService;
import com.yuansong.service.TaskWorkerManagerServiceImpl;
import com.yuansong.taskjob.TestJob;

@Controller
@RequestMapping(value="/")
public class RootController {

	private final Logger logger = Logger.getLogger(RootController.class);
	
	private final Gson mGson = new Gson();
	
	private String taskId;
	
	private interface FindTaskConfigResult{
		public void getConfig(IntTaskConfig config);
		public void getConfig(HealthTaskConfig config);
		public void getConfig(WebStateTaskConfig config);
	}
	
	@Autowired
	TaskWorkerManagerServiceImpl taskManager;
	
	@Autowired
	private ConfigService<IntTaskConfig> intTaskConfigService;
	
	@Autowired
	private ConfigService<HealthTaskConfig> healthTaskConfigService;
	
	@Autowired
	private ConfigService<WebStateTaskConfig> webStateTaskConfigService;
	
	@RequestMapping(value="/")
	public ModelAndView defaultPage(Map<String, Object> model){
		logger.debug("go to Current");
		return new ModelAndView("redirect:/Current");
	}
	
	@RequestMapping(value="/Current")
	public ModelAndView currentPage(Map<String, Object> model){
		logger.debug("RootController currentPage");
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		
		for(String key : taskManager.getTaskIdList()) {
			getConfig(key, new FindTaskConfigResult() {

				@Override
				public void getConfig(IntTaskConfig config) {
					Map<String, String> listItem = new HashMap<String, String>();
					listItem.put("id", config.getId());
					listItem.put("title", config.getTitle());
					listItem.put("remark", config.getRemark());
					listItem.put("type", "Int");
					listItem.put("configUrl", "/TaskConfig/Int/Detail/" + config.getId());
					list.add(listItem);
				}

				@Override
				public void getConfig(HealthTaskConfig config) {
					Map<String, String> listItem = new HashMap<String, String>();
					listItem.put("id", config.getId());
					listItem.put("title", config.getTitle());
					listItem.put("remark", config.getRemark());
					listItem.put("type", "Health");
					listItem.put("configUrl", "/TaskConfig/Health/Detail/" + config.getId());
					list.add(listItem);					
				}

				@Override
				public void getConfig(WebStateTaskConfig config) {
					Map<String, String> listItem = new HashMap<String, String>();
					listItem.put("id", config.getId());
					listItem.put("title", config.getTitle());
					listItem.put("remark", config.getRemark());
					listItem.put("type", "WebState");
					listItem.put("configUrl", "/TaskConfig/WebState/Detail/" + config.getId());
					list.add(listItem);
				}
				
			});
		}
		
		
		
//		for(IntTaskConfig config : intTaskConfigService.getConfigMap().values()) {
//			listItem = new HashMap<String, String>();
//			listItem.put("id", config.getId());
//			listItem.put("title", config.getTitle());
//			listItem.put("remark", config.getRemark());
//			listItem.put("type", "Int");
//			listItem.put("configUrl", "/TaskConfig/Int/Detail/" + config.getId());
//			list.add(listItem);
//		}
//		for(HealthTaskConfig config : healthTaskConfigService.getConfigMap().values()) {
//			listItem = new HashMap<String, String>();
//			listItem.put("id", config.getId());
//			listItem.put("title", config.getTitle());
//			listItem.put("remark", config.getRemark());
//			listItem.put("type", "Health");
//			listItem.put("configUrl", "/TaskConfig/Health/Detail/" + config.getId());
//			list.add(listItem);
//		}
//		for(WebStateTaskConfig config : webStateTaskConfigService.getConfigMap().values()) {
//			listItem = new HashMap<String, String>();
//			listItem.put("id", config.getId());
//			listItem.put("title", config.getTitle());
//			listItem.put("remark", config.getRemark());
//			listItem.put("type", "WebState");
//			listItem.put("configUrl", "/TaskConfig/WebState/Detail/" + config.getId());
//			list.add(listItem);
//		}
		model.put("list", list);
		
		List<String> menuList = new ArrayList<String>();
		menuList.add("Current");
		
		model.put("menulist", mGson.toJson(menuList));
		
		return new ModelAndView("CurrList", model);
	}
	
	@RequestMapping(value="/PageNotFound")
	public ModelAndView pageNotFound(Map<String, Object> model){
		logger.info("RootController PageNotFound");
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("errCode", "404");
		data.put("errDesc","Page not found.");
		
		model.put("info", mGson.toJson(data));
		
		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/testPage")
	public ModelAndView testPage(Map<String, Object> model){
		logger.info("RootController testPage");
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("errCode", "0");
		data.put("errDesc","");
		data.put("data", "testPage");
		
		model.put("info", mGson.toJson(data));

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/testPageOne")
	public ModelAndView testPageOne(Map<String, Object> model){
		logger.info("RootController testPageOne");
		
		Map<String,String> data = new HashMap<String,String>();
		data.put("errCode", "0");
		data.put("errDesc","");
		data.put("data", "testPageOne");
		
		model.put("info", mGson.toJson(data));

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/testErrorPage")
	public ModelAndView testErrorPage(Map<String, Object> model){
		logger.info("RootController testErrorPage");
		
		throw new RuntimeException("testErrorPage");
	}
	
	@RequestMapping(value="/addTask")
	public ModelAndView addTask(Map<String, Object> model) {
		taskId = CommonFun.UUID();
		taskManager.addTask(taskId, new TestJob(), "0/1 0/1 * * * ?");
		
		model.put("info", taskId);

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/stopTask")
	public ModelAndView stopTask(Map<String, Object> model) {
		
		taskManager.cancelTask(taskId);
		
		model.put("info", taskId);

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/SendMsg/{msg}")
	public ModelAndView stopTask(@PathVariable String msg, Map<String, Object> model) {
		
		DingMessageSender dms = new DingMessageSender("7a84d09b83f9633ad37866505d2c0c26e39f4fa916b3af8f6a702180d3b9906b");
		dms.send(msg);
		
		model.put("info", "Send Completed");

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/Http/Get")
	public ModelAndView httpGet(Map<String, Object> model) {
		
		logger.debug("RootController httpGet");
		
		HttpUtils httpUtils = new HttpUtils();
		String result = httpUtils.httpGet("http://123.57.70.114");
		
		model.put("info", result);

		return new ModelAndView("responsePage", model);
	}
	
	@RequestMapping(value="/Http/Post")
	public ModelAndView httpPost(Map<String, Object> model) {
		
		logger.debug("RootController httpPost");
		
		HttpUtils httpUtils = new HttpUtils();
		String result = httpUtils.httpPostJson("https://oapi.dingtalk.com/robot/send?access_token=7a84d09b83f9633ad37866505d2c0c26e39f4fa916b3af8f6a702180d3b9906b",
				"{\"msgtype\": \"text\",\"text\": {\"content\": \"" + "test" + "\" }}");
		
		model.put("info", result);

		return new ModelAndView("responsePage", model);
	}
	
	private void getConfig(String configId, FindTaskConfigResult findTaskConfigResult) {
		IntTaskConfig intTaskConfig = intTaskConfigService.getConfig(configId);
		if(intTaskConfig != null) {
			findTaskConfigResult.getConfig(intTaskConfig);
		}
		HealthTaskConfig healthTaskConfig = healthTaskConfigService.getConfig(configId);
		if(healthTaskConfig != null) {
			findTaskConfigResult.getConfig(healthTaskConfig);
		}
		WebStateTaskConfig webStateTaskConfig = webStateTaskConfigService.getConfig(configId);
		if(webStateTaskConfig != null) {
			findTaskConfigResult.getConfig(webStateTaskConfig);
		}
	}
	
}
