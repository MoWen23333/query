package smartlims.testresultmgtsvc.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smartlims.testresultmgtsvc.datamanager.AliquotInfo;
import smartlims.testresultmgtsvc.datamanager.JobInfo;
import smartlims.testresultmgtsvc.datamanager.LookupBriefItem;
import smartlims.testresultmgtsvc.datamanager.LookupCollection;
import smartlims.testresultmgtsvc.datamanager.LookupItem;
import smartlims.testresultmgtsvc.datamanager.LookupPassItem;
import smartlims.testresultmgtsvc.datamanager.LookupStateItem;
import smartlims.testresultmgtsvc.datamanager.ResultPackage_JSON;
import smartlims.testresultmgtsvc.enums.ResultEnums;
import smartlims.testresultmgtsvc.service.QueryService;
import smartlims.testresultmgtsvc.utils.DateUtils;

@RestController
@CrossOrigin
@RequestMapping(value ="/backend")
public class QueryController {

	@Autowired
	private QueryService queryService;

	@GetMapping(value = "/login")
	public ResultPackage_JSON getLogininfo(@RequestParam(value = "username") String loginBy) {
		// ArrayList<String> folderInfo = new ArrayList<>();
		if (queryService.CheckLoginByExist(loginBy)) {
			// 登录时获取查找表
			// queryService.runGetLookupCollection();
			// folderInfo = queryService.runGetLoginInfo(loginBy);
			queryService.runGetLoginInfo(loginBy);
			// 用户权限文件夹获取（暂不实现）
			// if (folderInfo != null) {
			// 	return new ResultPackage_JSON("ok", folderInfo);
			// } else {
			// 	return new ResultPackage_JSON("err", ResultEnums.LOGIN_INFO_GET_FAIL.getMessage());
			// }
			// return new ResultPackage_JSON("ok", ResultEnums.LOGIN_SUCCESS.getMessage());
			return new ResultPackage_JSON("ok", ResultEnums.LOGIN_SUCCESS.getMessage());
		}
		return new ResultPackage_JSON("err", ResultEnums.LOGIN_BY_NOT_FOUND.getMessage());			
	}
	
	@GetMapping(value = "/get_timeliness_rate")
	public ResultPackage_JSON getTimeLinessRate(@RequestParam(value = "date") String date) {
		ArrayList<LookupPassItem> rateList = new ArrayList<>();
		String sDate = DateUtils.convertFromDate2ValidDate(date);
		rateList = queryService.runGetTimeLinessSQL(sDate);
		if (rateList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", rateList);
        }
	}

	@GetMapping(value = "/get_finished_count")
	public ResultPackage_JSON getFinishedCount(@RequestParam(value = "date") String date) {
		ArrayList<LookupStateItem> countList = new ArrayList<>();
		String sDate = DateUtils.convertFromDate2ValidDate(date);
		countList = queryService.runGetFinishedCount(sDate);
		if (countList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", countList);
        }
	}

	@GetMapping(value = "/get_finished_count_xm")
	public ResultPackage_JSON getFinishedCountByXM(@RequestParam(value = "date") String date) {
		ArrayList<LookupStateItem> countList = new ArrayList<>();
		String sDate = DateUtils.convertFromDate2ValidDate(date);
		countList = queryService.runGetFinishedCountByProject(sDate);
		if (countList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", countList);
        }
	}

	@GetMapping(value = "/get_finished_rate")
	public ResultPackage_JSON getFinishedRate(@RequestParam(value = "date") String date) {
		ArrayList<LookupPassItem> countList = new ArrayList<>();
		String sDate = DateUtils.convertFromDate2ValidDate(date);
		countList = queryService.runGetFinishingRate(sDate);
		if (countList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", countList);
        }
	}
	
	@GetMapping(value = "/get_detail")
	public ResultPackage_JSON getAliquotDetailInfoById(@RequestParam(value = "id") String id) {
		AliquotInfo aliquotInfo = queryService.runGetAliquotInfoById(id);
		if (aliquotInfo == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", aliquotInfo);
        }
	}

	@GetMapping(value = "/get_unfinished_task")
	public ResultPackage_JSON getUnFinishedTask(@RequestParam(value = "name") String group,
												@RequestParam(value = "date") String date) {
		ArrayList<LookupBriefItem> taskList = new ArrayList<>();
		String sDate = DateUtils.convertFromDate2ValidDate(date);
		taskList = queryService.runGetNotFinishAliquotListByGroup(sDate, group, "");
		if (taskList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", taskList);
        }
	}

	// 测试
	@GetMapping(value = "/collection")
	public ResultPackage_JSON getLookupCollection() {
		LookupCollection lookupCollection;
		lookupCollection = queryService.runGetLookupCollection();
		if (lookupCollection == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", lookupCollection);
        }
	}	

	// 测试（按时间）
	@GetMapping(value = "/list") 
	public ResultPackage_JSON getAliquotInfo(@RequestParam(value = "fromDate") String sfromDate,
											 @RequestParam(value = "toDate") String stoDate) {
		ArrayList<LookupBriefItem> aliquotList = new ArrayList<>();
		String fromDate = DateUtils.convertFromDate2ValidDate(sfromDate);
		String toDate = DateUtils.convertFromDate2ValidDate(stoDate);
		aliquotList = queryService.runGetAliquotListByDate(fromDate, toDate);
		if (aliquotList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", aliquotList);
        }
	}

	@GetMapping(value = "joblist")
	public ResultPackage_JSON getJobList(@RequestParam(value = "fromDate") String sfromDate,
										 @RequestParam(value = "toDate") String stoDate) {
		ArrayList<JobInfo> jobList = new ArrayList<>();
		String fromDate = DateUtils.convertFromDate2ValidDate(sfromDate);
		String toDate = DateUtils.convertFromDate2ValidDate(stoDate);
		jobList = queryService.runGetJobListByDate(fromDate, toDate);
		if (jobList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", jobList);
        }
	}

	@GetMapping(value = "get_by_job")
	public ResultPackage_JSON getAliquotListByJob(@RequestParam(value = "name") String name) {
		ArrayList<LookupItem> aliquotList = queryService.runGetAliquotListByJobName(name);
		if (aliquotList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", aliquotList);
        }
	}

	// 测试（按时间）
	@GetMapping(value = "/unfinishlist") 
	public ResultPackage_JSON getAliquotInfo(@RequestParam(value = "date") String date,
											 @RequestParam(value = "group") String group,
											 @RequestParam(value = "testitem") String testitem) {
		ArrayList<LookupBriefItem> aliquotList = new ArrayList<>();

		aliquotList = queryService.runGetNotFinishAliquotListByGroup(date, group, testitem);
		if (aliquotList == null) {
			return new ResultPackage_JSON("err", ResultEnums.GET_DATA_FAIL.getMessage());
		}
		else {
			return new ResultPackage_JSON("ok", aliquotList);
        }
	}
}