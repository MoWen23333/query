package smartlims.testresultmgtsvc.service;

import java.util.ArrayList;

import smartlims.testresultmgtsvc.datamanager.AliquotInfo;
import smartlims.testresultmgtsvc.datamanager.JobInfo;
import smartlims.testresultmgtsvc.datamanager.LookupBriefItem;
import smartlims.testresultmgtsvc.datamanager.LookupCollection;
import smartlims.testresultmgtsvc.datamanager.LookupItem;
import smartlims.testresultmgtsvc.datamanager.LookupPassItem;
import smartlims.testresultmgtsvc.datamanager.LookupStateItem;


public interface QueryService {

    // 查找表
    // 查询LookupCollection
    LookupCollection runGetLookupCollection();

    // 登录信息
    ArrayList<String> runGetLoginInfo(String loginBy);
    Boolean CheckLoginByExist(String loginBy);

    // 获取时间范围内的分样列表{id, text, status, sqstatus}
    ArrayList<LookupBriefItem> runGetAliquotListByDate(String FromDate, String ToDate);    
    // 通过id获得分样详细信息
    AliquotInfo runGetAliquotInfoById(String id);

    // 实验室主任
    // 根据分组及检测项目获得未完成分样列表{id,text, status, sqstatus}
    ArrayList<LookupBriefItem> runGetNotFinishAliquotListByGroup(String date, String group, String testItem);
    // 查询已完成任务数量（常规测试项目与特殊测试项目统计）
    ArrayList<LookupStateItem> runGetFinishedCountByProject(String date);
    // 查询任务完成率数量（按照组和测试项目统计）
    ArrayList<LookupPassItem> runGetFinishingRate(String date);
    // 查询已完成任务数量（按统人计）
    ArrayList<LookupStateItem> runGetFinishedCount(String date);
    // 查询及时完成率（按分组）
    ArrayList<LookupPassItem> runGetTimeLinessSQL(String date);

    // 外部人员
    ArrayList<JobInfo> runGetJobListByDate(String FromDate, String ToDate);
    ArrayList<LookupItem> runGetAliquotListByJobName(String name);
}