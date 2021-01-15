package smartlims.testresultmgtsvc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import smartlims.testresultmgtsvc.datamanager.AliquotInfo;
import smartlims.testresultmgtsvc.datamanager.DataByProp;
import smartlims.testresultmgtsvc.datamanager.InstSubstanceInfo;
import smartlims.testresultmgtsvc.datamanager.InstrumentInfo;
import smartlims.testresultmgtsvc.datamanager.JobInfo;
import smartlims.testresultmgtsvc.datamanager.LookupBriefItem;
import smartlims.testresultmgtsvc.datamanager.LookupCollection;
import smartlims.testresultmgtsvc.datamanager.LookupItem;
import smartlims.testresultmgtsvc.datamanager.LookupPassItem;
import smartlims.testresultmgtsvc.datamanager.LookupStateItem;
import smartlims.testresultmgtsvc.service.QueryService;
import smartlims.testresultmgtsvc.utils.DateUtils;
import smartlims.testresultmgtsvc.utils.LookupItemUtils;
import smartlims.testresultmgtsvc.utils.PropUtils;

@Service
@Slf4j
public class QueryServiceImpl implements QueryService {
    private static JdbcTemplate jdbcTemplate;
    private static final ArrayList<String> groupIdList = new ArrayList<>();
    private static String operatorId = "";
    private static String operatorName = "";

    // 样品、分样
    private static String aliquotFields = "id_numeric, qt_subgroup, csxdl_name, client_user, starter, completer, product_text, test_gather, "
            + "date_result_required, id_text, status, login_by, authoriser, login_date, date_completed, sqstatus";
    private static String aliquotBreifFields = "id_numeric, id_text, status, sqstatus";
    private static String instrumentFieldsInfo = "identity, name, fixedasset_num, status, install_site, incharge_by, tech_chargeby";
    private static String jclbCommon = "where (a.parent_sample = b.id_numeric) and (a.parent_sample <> 0)";

    private ArrayList<LookupItem> staffs;
    private ArrayList<LookupStateItem> sampleStatus;
    private ArrayList<LookupStateItem> jobStatus;
    private ArrayList<LookupStateItem> sqStatus;
    private ArrayList<LookupItem> instStatus;
    private ArrayList<LookupItem> test;
    private ArrayList<LookupItem> group;
    private ArrayList<LookupItem> applicant;

    public static void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        QueryServiceImpl.jdbcTemplate = jdbcTemplate;
    }

    // 获取分组列表
    private ArrayList<LookupItem> runGetGroupSQL() {
        return runGetPhraseSQL("QTLC_GROUP");
    }

    // 获取人员信息列表
    private ArrayList<LookupItem> runGetStaffSQL() {
        // group_id为C为“作废”
        return runGetIDValueSQL(
                "select identity, name from PERSONNEL where removeflag = 'F' and group_id <> 'C' and group_id <> 'SYSTEST' order by identity",
                "identity", "name");
    }

    // 获取申请状态列表
    private ArrayList<LookupStateItem> runGetJobStatusSQL() {
        return DataByProp.getStatusOrder("status.job.id", "status.job.text", "status.job.order");
    }

    // 获取样品信息状态列表
    private ArrayList<LookupStateItem> getSampleStatusSQL() {
        return DataByProp.getStatusOrder("status.sample.id", "status.sample.text", "status.sample.order");
    }

    // 获得sq状态列表
    private ArrayList<LookupStateItem> runGetSqStatusSQL() {
        return DataByProp.getStatusOrder("status.sq.id", "status.sq.text", "status.sq.order");
    }

    // 获得仪器状态列表
    private ArrayList<LookupItem> runGetInstStatusSQL() {
        return runGetPhraseSQL("INST_STAT");
    }

    // 获得测试项目列表
    private ArrayList<LookupItem> runGetTestSQL() {
        return runGetIDValueSQL(
                "select identity, name from VERSIONED_ANALYSIS where removeflag = 'F' order by identity", "identity",
                "name");
    }

    private ArrayList<LookupItem> runGetApplicantSQL() {
        return runGetIDValueSQL("select distinct client_user from JOB_HEADER", "client_user", "client_user");
    }

    // 获得登录人id
    private String runGetOperatorId(String loginBy) {
        String sSQLString = "select identity from personnel where name = '" + loginBy + "'";
        try {
            return jdbcTemplate.queryForMap(sSQLString).get("identity").toString();
        } catch (final Exception e) {
            log.error("【runGetOperatorId】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    // 根据时间以及样本/分样，获得{id,name}列表
    private ArrayList<LookupBriefItem> runGetListByDate(final String fromDate, final String toDate,
            final Boolean IsAliquot) {
        String sSqlString = "";
        String sFromDate = DateUtils.convertFromDate2ValidDate(fromDate);
        String sToDate = DateUtils.convertToDate2ValidDate(toDate);
        if (fromDate.compareTo(toDate) == 1) {
            // 如果开始时间大于结束时间，则交换
            final String temp = sFromDate;
            sFromDate = sToDate;
            sToDate = temp;
            log.error("【runGetListByDate】时间不符合规范，原时间为：fromDate=" + fromDate + ",toDate=" + toDate + ";修改后时间为：formDate="
                    + sFromDate + ", toDate=" + sToDate);
        }
        if (IsAliquot) {
            sSqlString = "select id_numeric, id_text, status, sqstatus from SAMPLE where "
                    + ConstructAliquotWhereClause("T", sFromDate, sToDate, "login_date", "", "");
        } else {
            sSqlString = "select id_numeric, id_text, status, sqstatus from SAMPLE where "
                    + ConstructAliquotWhereClause("F", sFromDate, sToDate, "login_date", "", "");
        }
        return runGetID3ValueSQL(sSqlString, "id_numeric", "id_text", "status", "sqstatus");
    }

    // 获取{ID，Value}数据对列表
    private static ArrayList<LookupItem> runGetIDValueSQL(final String sSQLString, final String idFieldName,
            final String valueFieldName) {

        final ArrayList<LookupItem> lookupItemList = new ArrayList<LookupItem>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : list) {
                lookupItemList.add(new LookupItem(map.get(idFieldName).toString().trim(),
                        map.get(valueFieldName).toString().trim()));
            }
            return lookupItemList;

        } catch (final Exception e) {
            log.error("【runGetIDValueSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    // 获取{ID, Value1, Value2}数据对列表
    private static ArrayList<LookupStateItem> runGetID2ValueSQL(final String sSQLString, final String idFieldName,
            final String value1FieldName, final String value2FieldName) {

        final ArrayList<LookupStateItem> lookupItemList = new ArrayList<>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : list) {
                lookupItemList.add(new LookupStateItem(map.get(idFieldName).toString().trim(),
                        map.get(value1FieldName).toString().trim(), map.get(value2FieldName).toString().trim()));
            }
            return lookupItemList;

        } catch (final Exception e) {
            log.error("【runGetID2ValueSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    // 获取{ID，Value1, Value2, Value3}数据对列表
    private ArrayList<LookupBriefItem> runGetID3ValueSQL(final String sSQLString, final String idFieldName,
            final String value1FieldName, final String value2FieldName, final String value3FieldName) {

        final ArrayList<LookupBriefItem> lookupNewItemList = new ArrayList<>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : list) {
                lookupNewItemList.add(new LookupBriefItem(map.get(idFieldName).toString().trim(),
                        map.get(value1FieldName).toString().trim(),
                        LookupItemUtils.findLookupStateItemById(map.get(value2FieldName).toString().trim(),
                                sampleStatus),
                        LookupItemUtils.findLookupStateItemById(map.get(value3FieldName).toString().trim(), sqStatus)));
            }
            return lookupNewItemList;

        } catch (final Exception e) {
            log.error("【runGetID3ValueSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    // 获取{ID，Value1, Value2, Value3, Value4, Value5}数据对列表
    private ArrayList<LookupPassItem> runGetID5ValueSQL(final String sSQLString, final String groupFieldName,
            final String itemFieldName, final String allFieldName, final String passFieldName,
            final String unPassFieldName, final String rateFieldName) {
        final ArrayList<LookupPassItem> LookupPassItemList = new ArrayList<>();
        try {
            if (itemFieldName.equals("")) {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
                for (Map<String, Object> map : list) {
                    LookupPassItemList.add(new LookupPassItem(map.get(groupFieldName).toString().trim(), "",
                            map.get(allFieldName).toString().trim(), map.get(passFieldName).toString().trim(),
                            map.get(unPassFieldName).toString().trim(), map.get(rateFieldName).toString().trim()));
                }
            } else {
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
                for (Map<String, Object> map : list) {
                    LookupPassItemList.add(new LookupPassItem(map.get(groupFieldName).toString().trim(),
                            map.get(itemFieldName).toString().trim(), map.get(allFieldName).toString().trim(),
                            map.get(passFieldName).toString().trim(), map.get(unPassFieldName).toString().trim(),
                            map.get(rateFieldName).toString().trim()));
                }
            }
        } catch (final Exception e) {
            log.error("【runGetID5ValueSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
        return LookupPassItemList;
    }

    // 获取某个字段值的列表
    private static ArrayList<String> runSinglerValueListSQL(String sFieldName, String sTableName, String whereClause) {
        final ArrayList<String> idList = new ArrayList<>();
        final String sSQLString = "select distinct " + sFieldName + " from " + sTableName + whereClause;
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : list) {
                idList.add(map.get(sFieldName).toString().trim());
            }
        } catch (final Exception e) {
            log.error("【runSinglerValueListSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
        return idList;
    }

    // 从短语中获取列表
    private static ArrayList<LookupItem> runGetPhraseSQL(String sphraseType) {
        String sSQLString = "select phrase_id, phrase_text from PHRASE where "
                + ConstructAliquotWhereClause("", "", "", "phrase_id", "phrase_type", sphraseType);
        return runGetIDValueSQL(sSQLString, "phrase_id", "phrase_text");
    }

    // 根据条件，获得过滤字段后的Map<String, Object>
    private static List<Map<String, Object>> runGetDesignatedMapList(final String sFieldNames, final String tableName,
            final String whereClause) {
        String sSQLString = "";
        if (whereClause.isEmpty()) {
            sSQLString = "select " + sFieldNames + " from " + tableName;
        } else {
            sSQLString = "select " + sFieldNames + " from " + tableName + " where " + whereClause;
        }
        try {
            return jdbcTemplate.queryForList(sSQLString);
        } catch (final Exception e) {
            log.error("【runGetDesignatedMapList】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    // 获取仪器详细列表
    public ArrayList<InstrumentInfo> runGetInstrumentInfos(final String sCondition) {
        ArrayList<InstrumentInfo> instrumentList = new ArrayList<>();
        List<Map<String, Object>> mapList = runGetDesignatedMapList(instrumentFieldsInfo, "INSTRUMENT", sCondition);
        if (mapList == null) {
            log.info("【runGetInstrumentInfos】map获取为空");
            return null;
        }
        mapList.forEach(map -> instrumentList.add(new InstrumentInfo(map.get("identity").toString(),
                map.get("name").toString(), map.get("fixedasset_num").toString(),
                LookupItemUtils.findLookupItemById(map.get("status").toString(), instStatus),
                map.get("install_site").toString(),
                LookupItemUtils.findLookupItemById(map.get("incharge_by").toString(), staffs),
                LookupItemUtils.findLookupItemById(map.get("tech_chargeby").toString(), staffs))));
        return instrumentList;
    }

    // 获得分样详细信息
    private ArrayList<AliquotInfo> getAliquotDetailListByCondition(String sCondition) {
        ArrayList<AliquotInfo> aliquotList = new ArrayList<>();
        List<Map<String, Object>> mapList = runGetDesignatedMapList(aliquotFields, "SAMPLE", sCondition);
        if (mapList == null) {
            log.info("【getAliquotDetailListByCondition】map获取为空");
            return null;
        }
        try {
            mapList.forEach(map -> {
                String dateResultRequired = "";
                String dateCompleted = "";
                String loginDate = "";
                // 时间数据库没有默认值，可能为null
                if (map.get("date_result_required") != null) {
                    dateResultRequired = map.get("date_result_required").toString();
                }
                if (map.get("login_date") != null) {
                    loginDate = map.get("login_date").toString();
                }
                if (map.get("date_completed") != null) {
                    dateCompleted = map.get("date_completed").toString();
                }

                aliquotList.add(new AliquotInfo(map.get("id_numeric").toString().trim(),
                        map.get("qt_subgroup").toString().trim(), map.get("csxdl_name").toString().trim(),
                        map.get("client_user").toString().trim(),
                        LookupItemUtils.findLookupItemById(map.get("starter").toString().trim(), staffs),
                        LookupItemUtils.findLookupItemById(map.get("completer").toString().trim(), staffs),
                        map.get("product_text").toString(), map.get("test_gather").toString().trim(),
                        dateResultRequired, map.get("id_text").toString().trim(),
                        LookupItemUtils.findLookupStateItemById(map.get("status").toString().trim(), sampleStatus),
                        LookupItemUtils.findLookupItemById(map.get("login_by").toString().trim(), staffs),
                        LookupItemUtils.findLookupItemById(map.get("authoriser").toString().trim(), staffs), loginDate,
                        dateCompleted,
                        LookupItemUtils.findLookupStateItemById(map.get("sqstatus").toString().trim(), sqStatus)));
            });
        } catch (Exception e) {
            log.error("【getAliquotDetailListByCondition】异常，aliquotlist={}", aliquotList);
        }
        return aliquotList;
    }

    // 构造条件句
    // 参数：样品/分样，开始时间，结束时间，按照field排序，查找某field，该field的值
    private static String ConstructAliquotWhereClause(String isSplit, String sFromDate, String sToDate,
            String orderFieldName, String sFieldName, String sFieldValue) {
        String result = "";
        if (isSplit != null && isSplit.length() != 0) {
            result = result + "(is_split = '" + isSplit + "') ";
        }
        if (sFromDate != null && sFromDate.length() != 0 && sToDate != null && sToDate.length() != 0) {
            String sDateClause = "(login_date >= Date'" + sFromDate + "') and (login_date <= Date'" + sToDate + "') ";
            if (result.length() != 0) {
                result = result + "and " + sDateClause;
            } else {
                result = result + sDateClause;
            }
        }
        if (sFieldName != null && sFieldName.length() != 0 && sFieldValue != null && sFieldValue.length() != 0) {
            String sFieldClause = sFieldName + "= '" + sFieldValue + "'";
            if (result.length() != 0) {
                result = result + " and " + sFieldClause;
            } else {
                result = result + sFieldClause;
            }
        }
        if (orderFieldName != null && orderFieldName.length() != 0) {
            result = result + " order by " + orderFieldName + " desc";
        }
        return result;
    }

    // 两表join SQL语句拼接
    private String ConstructJoinSQL(String tableAFiled, String tableA, String tableBFiled, String tableB,
            String folderName, String bStatus, String aStatus, String aSqStatus) {
        String sAFeild = SplitFiled("a.", tableAFiled);
        String sBFeild = SplitFiled("b.", tableBFiled);
        if (!sBFeild.equals("") && !sAFeild.equals("")) {
            sAFeild = sAFeild + "," + sBFeild;
        }
        Map<String, String> proMap = PropUtils.getPropByFolderName(folderName);
        if (proMap == null)
            return "";
        else {
            String sampleType = proMap.get("sampletype");
            String testItemType = proMap.get("testitemtype");
            String csxmDescription = proMap.get("csxmdescription");
            String sCondition = ConstructJCLBWhereClause(bStatus, aStatus, aSqStatus, sampleType, testItemType,
                    csxmDescription, "id_numeric");
            String sSQLString = "select " + sAFeild + " from " + tableA + " a, " + tableB + " b " + jclbCommon
                    + sCondition;
            return sSQLString;
        }
    }

    // 拼接filed
    private String SplitFiled(String flag, String SFiled) {
        String result = "";
        if (SFiled == "") {
            return "";
        }
        String[] splitA = SFiled.split(",");
        for (int i = 0; i < splitA.length - 1; i++) {
            result = result + flag + splitA[i].trim() + ',';
        }
        result = result + flag + splitA[splitA.length - 1].trim();
        return result;
    }

    // 构造检测列表的查询语句
    private String ConstructJCLBWhereClause(String parentStatus, String status, String sqStatus, String sampleType,
            String testItemType, String csxmDes, String orderFiled) {
        String result = "";
        String[] split;
        if (!parentStatus.equals("")) {
            result = result + " and (b.status = '" + parentStatus + "')";
        }
        if (!status.equals("")) {
            result = result + " and (a.status = '" + status + "')";
        }
        if (!sqStatus.equals("")) {
            result = result + " and (a.sqstatus = '" + sqStatus + "')";
        }
        if (!sampleType.equals("")) {
            result = result + " and (a.samptype1 = '" + sampleType + "')";
        }
        if (!testItemType.equals("")) {
            result = result + " and (a.testitemtype = '" + testItemType + "')";
        }
        if (!csxmDes.equals("")) {
            split = csxmDes.split(",");
            if (split.length > 1) {
                for (Integer i = 0; i < split.length - 1; i++) {
                    result = result + " and ((a.csxm_description = '" + split[i] + "') or ";
                }
                result = result + " (a.csxm_description = '" + split[split.length - 1] + "'))";
            } else {
                result = result + " (a.csxm_description = '" + split[split.length - 1] + "')";
            }

        }
        if (!orderFiled.equals("")) {
            result = result + " order by a." + orderFiled + " desc";
        }
        return result;
    }

    // 构造关于group_id查找的语句
    private String ConstructFindGroupIDClause() {
        String result = "";
        if (!groupIdList.isEmpty()) {
            result = " and (";
        }
        for (int i = 0; i < groupIdList.size() - 1; i++) {
            result = result + "(group_id = '" + groupIdList.get(i) + "') or ";
        }
        result = result + "(group_id = '" + groupIdList.get(groupIdList.size() - 1) + "'))";
        return result;
    }

    // 构造列表的语句
    private String ConstructListClause(String fileds, String sSampleType, String sLoginDate, String sAuthoriseDate,
            Boolean isFinished, String subGroup, String sTestItem) {
        String result = "";
        result = "select " + fileds + " from sample" + ConstructListWhereClause(sSampleType, sLoginDate, sAuthoriseDate,
                isFinished, subGroup, sTestItem, "");
        return result;
    }

    // 构造列表条件语句
    private String ConstructListWhereClause(String sSampleType, String sLoginDate, String sAuthoriseDate,
            Boolean isFinished, String subGroup, String sTestItem, String flag) {
        String[] typeSplit;
        String result = " where (" + flag + "parent_sample <> 0)";
        if (!sSampleType.equals("")) {
            typeSplit = sSampleType.split(",");
            if (typeSplit.length > 1) {
                for (Integer i = 0; i < typeSplit.length - 1; i++) {
                    result = result + " and (" + flag + "samptype1 = '" + typeSplit[i] + "' or ";
                }
                result = result + " " + flag + "samptype1 = '" + typeSplit[typeSplit.length - 1] + "')";
            } else {
                result = result + " and (" + flag + "samptype1 = '" + typeSplit[typeSplit.length - 1] + "')";
            }
        }
        if (!sLoginDate.equals("")) {
            result = result + " and (" + flag + "login_date >= Date'" + sLoginDate + "')";
        }
        if (!sAuthoriseDate.equals("")) {
            result = result + " and (" + flag + "date_authorised >= Date'" + sAuthoriseDate + "') ";
        }
        if (isFinished) {
            result = result + " and (" + flag + "status = 'A')";
        }
        if (!isFinished) {
            result = result + " and ((" + flag + "status <> 'A') and (" + flag + "status <> 'X') and (" + flag
                    + "status <> 'R'))";
        }
        if (!subGroup.equals("")) {
            result = result + " and (" + flag + "qt_subgroup = '" + subGroup + "')";
        }
        if (!sTestItem.equals("")) {
            result = result + " and (" + flag + "csxdl_name = '" + sTestItem + "')";
        }
        return result;
    }

    private String ConstructExcludeSQL(String identityField) {
        String result = "";
        String[] idSplit;
        idSplit = PropUtils.getPropString("exclude.person").split(",");
        for (String id : idSplit) {
            if (id.toUpperCase().equals("SYSTEM")) {
                result = result + " and UPPER(trim(" + identityField + "))<>'SYSTEM'";
            } else {
                result = result + " and trim(" + identityField + ") <> '" + id + "'";
            }
        }
        return result;
    }

    private String ConstructGroupPhrase(String groupBy, String orderBy) {
        String result = "";
        if (!groupBy.equals(",")) {
            result = " group by " + groupBy;
        }
        if (!orderBy.equals("")) {
            result = result + " order by " + orderBy;
        }
        return result;
    }

    // 获得仪器标准物质列表（无数量）
    private ArrayList<InstSubstanceInfo> runGetSubstanceInfos() {
        ArrayList<InstSubstanceInfo> instSubstanceList = new ArrayList<>();
        List<Map<String, Object>> mapList = runGetDesignatedMapList("*", "INSTSUBSTANCE", "");
        if (mapList == null) {
            log.info("【runGetSubstanceInfos】map获取为空");
            return null;
        }
        try {
            mapList.forEach(map -> {
                String dateDZ = "";
                String dateYXQ = "";
                // 时间数据库没有默认值，可能为null
                if (map.get("date_dz") != null) {
                    dateDZ = map.get("date_dz").toString();
                }
                if (map.get("date_yxq") != null) {
                    dateYXQ = map.get("date_yxq").toString();
                }
                instSubstanceList.add(new InstSubstanceInfo(map.get("identity").toString(),
                        map.get("group_id").toString(), map.get("name").toString(), map.get("ggxh").toString(),
                        map.get("bzwzbh").toString(), map.get("bzz").toString(), map.get("xdkzbqdd").toString(), dateDZ,
                        dateYXQ, map.get("tgdw").toString(), map.get("note").toString()));
            });
        } catch (Exception e) {
            log.error("【runGetSubstanceInfos】异常，instSubstanceList={}", instSubstanceList);
        }
        return instSubstanceList;
    }

    // 获得仪器的详细信息
    private ArrayList<InstrumentInfo> runGetInstrumentDetailInfos() {
        return runGetInstrumentInfos("removeflag = 'F'");
    }

    // 获得某一分组下的仪器详细信息（分组由登录人决定）
    private ArrayList<InstrumentInfo> runGetInstrumentInfosByGroup() {
        return runGetInstrumentInfos("removeflag = 'F'" + ConstructFindGroupIDClause());
    }

    // 获得待接收分样列表（根据foldername查找）
    private ArrayList<LookupBriefItem> runGetReceiveAliquotList(String folderName) {
        ArrayList<LookupBriefItem> aliquotList = new ArrayList<>();
        if (folderName.equals("")) {
            log.error("【runGetReceiveAliquotList】folderName为空，operatorId=", operatorId);
            return null;
        } else {
            String sSQLString = ConstructJoinSQL(aliquotFields, "SAMPLE", "status", "SAMPLE", folderName, "V", "U", "");
            aliquotList = runGetID3ValueSQL(sSQLString, "id_numeric", "id_text", "status", "sqstatus");
            return aliquotList;
        }
    }

    // 获得待测试分样列表（根据foldername查找）
    private ArrayList<LookupBriefItem> runGetTestAliquotList(String folderName) {
        ArrayList<LookupBriefItem> aliquotList = new ArrayList<>();
        if (folderName.equals("")) {
            log.error("【runGetTestAliquotList】folderName为空，operatorId=", operatorId);
            return null;
        } else {
            String sSQLString = ConstructJoinSQL(aliquotFields, "SAMPLE", "status", "SAMPLE", folderName, "", "V", "");
            aliquotList = runGetID3ValueSQL(sSQLString, "id_numeric", "id_text", "status", "sqstatus");
            return aliquotList;
        }
    }

    // 获得待审核分样列表（根据foldername查找）
    private ArrayList<LookupBriefItem> runGetAuthoriseAliquotList(String folderName) {
        ArrayList<LookupBriefItem> aliquotList = new ArrayList<>();
        if (folderName.equals("")) {
            log.error("【runGetAuthoriseAliquotList】folderName为空，operatorId=", operatorId);
            return null;
        } else {
            String sSQLString = ConstructJoinSQL(aliquotFields, "SAMPLE", "status", "SAMPLE", folderName, "", "C",
                    "DS");
            aliquotList = runGetID3ValueSQL(sSQLString, "id_numeric", "id_text", "status", "sqstatus");
            return aliquotList;
        }
    }

    // 获得期间审核的仪器信息
    private ArrayList<InstrumentInfo> runGetVeriInstrumentList() {
        Integer veriSubDate = PropUtils.getPropInteger("veriDate");
        String sVeriDate = DateUtils.substractDate(veriSubDate);
        String sWhereClause = "(nextverificat <= to_date('" + sVeriDate
                + ",'yyyy/mm/dd HH24:MI:SS')) and ((incharge_by = '" + operatorId + "') or (techcharge_by = '"
                + operatorId + "')) and veri_stat = 'W'";
        return runGetInstrumentInfos(sWhereClause);
    }

    // 获得校准的仪器信息
    private ArrayList<InstrumentInfo> runGetCalibInstrumentList() {
        Integer calibSubDate = PropUtils.getPropInteger("calibDate");
        String sCalibDate = DateUtils.substractDate(calibSubDate);
        String sWhereClause = "(next_calib_date <= to_date('" + sCalibDate
                + ",'yyyy/mm/dd HH24:MI:SS')) and ((incharge_by = '" + operatorId + "') or (techcharge_by = '"
                + operatorId + "')) and calib_stat = 'W'";
        return runGetInstrumentInfos(sWhereClause);
    }

    // 获取登录信息（该用户的group，samplemanager中可以查询的folder）
    @Override
    public ArrayList<String> runGetLoginInfo(String loginBy) {
        ArrayList<String> folderName = new ArrayList<>();
        String sSQLString = "select distinct b.group_id from personnel a, grouplink b where (select identity from personnel where name = '"
                + loginBy + "') = b.operator_id";
        groupIdList.clear();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : list) {
                groupIdList.add(map.get("group_id").toString());
            }
            if (!groupIdList.isEmpty()) {
                folderName.clear();
                for (String group : groupIdList) {
                    sSQLString = "select name from EXPLORER_FOLDER t where group_id = '" + group
                            + "' and cabinet = 'USER_QTYPSJ'";
                    List<Map<String, Object>> folderList = jdbcTemplate.queryForList(sSQLString);
                    for (Map<String, Object> map : folderList) {
                        folderName.add(map.get("name").toString());
                    }
                }
            }
        } catch (final Exception e) {
            log.error("【runGetLoginInfo】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
        operatorId = runGetOperatorId(loginBy);
        return folderName;
    }

    // 登录人检验
    @Override
    public Boolean CheckLoginByExist(String loginBy) {
        operatorName = loginBy;
        runGetLookupCollection();
        try {
            // 目前只允许实验室主任登录
            String sSQLString = "select * from grouplink where operator_id = (select identity from personnel where name = '"
                    + loginBy + "' and removeflag = 'F') and group_id = 'GL'";
            jdbcTemplate.queryForMap(sSQLString);
        } catch (Exception e) {
            log.error("【CheckLoginByExist】异常，登录人信息错误，loginBy={}", loginBy);
            return false;
        }
        return true;
    }

    // 获取LookupCollection
    @Override
    public LookupCollection runGetLookupCollection() {

        staffs = runGetStaffSQL();
        sampleStatus = getSampleStatusSQL();
        jobStatus = runGetJobStatusSQL();
        sqStatus = runGetSqStatusSQL();
        instStatus = runGetInstStatusSQL();
        group = runGetGroupSQL();
        test = runGetTestSQL();
        applicant = runGetApplicantSQL();

        if (staffs == null) {
            log.error("【runGetLookupCollection】staff获取失败");
            return null;
        }

        if (sampleStatus == null) {
            log.error("【runGetLookupCollection】sampleStatus获取失败");
            return null;
        }

        if (jobStatus == null) {
            log.error("【runGetLookupCollection】jobStatus获取失败");
            return null;
        }

        if (sqStatus == null) {
            log.error("【runGetLookupCollection】sqStatus获取失败");
            return null;
        }

        if (instStatus == null) {
            log.error("【runGetLookupCollection】instStatus获取失败");
            return null;
        }

        if (group == null) {
            log.error("【runGetLookupCollection】group获取失败");
            return null;
        }

        if (test == null) {
            log.error("【runGetLookupCollection】test获取失败");
            return null;
        }

        if (applicant == null) {
            log.error("【runGetLookupCollection】applicant获取失败");
            return null;
        }

        final LookupCollection lookupCollection = new LookupCollection(staffs, sampleStatus, jobStatus, sqStatus,
                instStatus, group, test, applicant);
        return lookupCollection;
    }

    // 根据时间获得分样列表
    @Override
    public ArrayList<LookupBriefItem> runGetAliquotListByDate(final String FromDate, final String ToDate) {
        return runGetListByDate(FromDate, ToDate, true);
    }

    // 根据ID获取aliquotInfo
    @Override
    public AliquotInfo runGetAliquotInfoById(final String id) {
        String sCondition = "trim(id_numeric) = " + id;
        return getAliquotDetailListByCondition(sCondition).get(0);
    }

    // 根据分组获得没有完成的分样列表
    @Override
    public ArrayList<LookupBriefItem> runGetNotFinishAliquotListByGroup(final String date, final String group,
            final String testItem) {
        String sSqlString = "";
        sSqlString = ConstructListClause(aliquotBreifFields, "JCYP,TSJCYP", date, "", false, group, testItem);
        return runGetID3ValueSQL(sSqlString, "id_numeric", "id_text", "status", "sqstatus");
    }

    // 根据测试项目获得完成数量（SAMPTYPE1区分是检测样品还是特殊检测样品）
    @Override
    public ArrayList<LookupStateItem> runGetFinishedCountByProject(final String date) {
        ArrayList<LookupStateItem> unFinishedList = new ArrayList<>();
        final String sSQLString = ConstructListClause("csxdl_name,count(*) as rwnumber,SAMPTYPE1", "TSJCYP,JCYP", date,
                date, true, "", "") + ConstructGroupPhrase("csxdl_name,SAMPTYPE1", "SAMPTYPE1");
        unFinishedList = runGetID2ValueSQL(sSQLString, "csxdl_name", "rwnumber", "SAMPTYPE1");
        return unFinishedList;
    }

    // 获得完成率及相关数量
    @Override
    public ArrayList<LookupPassItem> runGetFinishingRate(String date) {
        ArrayList<LookupPassItem> finishingList = new ArrayList<>();
        final String sSQLString = "with a as (select s.id_numeric as sampleid,s.status, case when s.status='A' then 'T' else case when s.status <> 'A' "
                + "then case when s.status <> 'X' then case when s.status<>'R' then 'F' end end end end wc, s.qt_subgroup,s.csxdl_name from sample s "
                + "where s.login_date>=Date'" + date
                + "' and trim(s.parent_sample)<>'0' and (s.samptype1='JCYP' or s.samptype1='TSJCYP') and s.status <> 'X' and s.status <> 'R'),"
                + "t as (select qt_subgroup,csxdl_name,count(sampleid) num from a group by qt_subgroup, csxdl_name),"
                + "ywc as (select qt_subgroup,csxdl_name,count(sampleid) num from a where wc='T' group by qt_subgroup, csxdl_name),"
                + "wwc as (select qt_subgroup,csxdl_name,count(sampleid) num from a where wc='F' group by qt_subgroup, csxdl_name),"
                + "g as (select t.qt_subgroup,t.csxdl_name,t.num allcount,nvl(ywc.num,0) wccount,nvl(wwc.num,0) "
                + "wwccount from t,ywc,wwc where t.qt_subgroup=ywc.qt_subgroup(+) and t.qt_subgroup=wwc.qt_subgroup(+) and t.csxdl_name=ywc.csxdl_name(+) "
                + "and t.csxdl_name=wwc.csxdl_name(+)) select qt_subgroup,csxdl_name,allcount,wccount,wwccount,round(wccount/allcount*100,1) wcl from g "
                + "order by qt_subgroup";
        finishingList = runGetID5ValueSQL(sSQLString, "qt_subgroup", "csxdl_name", "allcount", "wccount", "wwccount",
                "wcl");
        return finishingList;
    }

    // 根据完成人统计完成数量
    @Override
    public ArrayList<LookupStateItem> runGetFinishedCount(String date) {
        ArrayList<LookupStateItem> finishedList = runGetFinishedCountByTestPerson(date);
        ArrayList<LookupStateItem> finishedFinishList = runGetFinishedCountByFinishPerson(date);
        finishedList.addAll(finishedFinishList);
        return finishedList;
    }

    // 统计检测样品的测试人
    private ArrayList<LookupStateItem> runGetFinishedCountByTestPerson(String date) {
        ArrayList<LookupStateItem> finishedList = new ArrayList<>();
        final String sCondition = " and r.test_number = t.test_number and t.sample = s.id_numeric and (r.entered_by=p.identity or r.entered_by= p.name)";
        final String sSQLString = "select p.name as name,count(distinct(s.id_text)) as rwnumber,s.SAMPTYPE1 from result r,test t, sample s,personnel p "
                + ConstructListWhereClause("JCYP", date, date, true, "", "", "s.") + sCondition
                + ConstructExcludeSQL("p.identity") + ConstructGroupPhrase("p.name, SAMPTYPE1", "");
        finishedList = runGetID2ValueSQL(sSQLString, "name", "rwnumber", "SAMPTYPE1");
        return finishedList;
    }

    // 统计特殊检测样品的完成人
    private ArrayList<LookupStateItem> runGetFinishedCountByFinishPerson(String date) {
        ArrayList<LookupStateItem> finishedList = new ArrayList<>();
        final String sCondition = " and s.completer=p.identity";
        final String sSQLString = "select p.name as name,count(distinct(s.id_text)) as rwnumber,s.SAMPTYPE1 from sample s,personnel p "
                + ConstructListWhereClause("TSJCYP", date, date, true, "", "", "s.") + sCondition
                + ConstructExcludeSQL("p.identity") + ConstructGroupPhrase("p.name, SAMPTYPE1", "");
        finishedList = runGetID2ValueSQL(sSQLString, "name", "rwnumber", "SAMPTYPE1");
        return finishedList;
    }

    // 获取及时率及其相关数量
    @Override
    public ArrayList<LookupPassItem> runGetTimeLinessSQL(String date) {
        ArrayList<LookupPassItem> list = runGetTimeLinessBySubgroupSQL(date);
        list.addAll(runGetTotalTimeLinessSQL(date));
        return list;
    }

    // 根据分组分别计算及时率
    private ArrayList<LookupPassItem> runGetTimeLinessBySubgroupSQL(String date) {
        ArrayList<LookupPassItem> list = new ArrayList<>();
        final String sSQLString = "with v_smp_comp_date as ( select s.id_numeric as sampleid,s.id_text,s.status,to_char(s.date_authorised,'yyyy-mm-dd'),"
                + "to_char(s.date_result_required,'yyyy-mm-dd'), s.is_split, case when s.status='A' then case when s.date_authorised<=s.date_result_required"
                + " then 'T' else 'F' end else case when s.date_result_required>=sysdate then 'T' else 'F' end end wc, s.qt_subgroup from sample s where "
                + "s.login_date>=date'" + date
                + "' and trim(s.parent_sample)<>'0' and s.status not in ('X','R') and (s.samptype1='JCYP' or s.samptype1='TSJCYP')),"
                + " t as (select qt_subgroup,count(sampleid) num from v_smp_comp_date group by qt_subgroup), js as (select qt_subgroup,count(sampleid) num from "
                + "v_smp_comp_date where wc='T' group by qt_subgroup), bjs as (select qt_subgroup,count(sampleid) num from v_smp_comp_date where wc='F' group by "
                + "qt_subgroup), g as (select t.qt_subgroup,t.num allcount,nvl(js.num,0) jscount,nvl(bjs.num,0) bjscount from t,js,bjs where "
                + "t.qt_subgroup=js.qt_subgroup(+) and t.qt_subgroup=bjs.qt_subgroup(+)) select qt_subgroup as subgroup,allcount,jscount,bjscount,round(jscount/allcount*100,1) jsl from g "
                + "order by subgroup";
        list = runGetID5ValueSQL(sSQLString, "subgroup", "", "allcount", "jscount", "bjscount", "jsl");
        return list;
    }

    // 计算总的及时率
    private ArrayList<LookupPassItem> runGetTotalTimeLinessSQL(String date) {
        ArrayList<LookupPassItem> list = new ArrayList<>();
        final String sSQLString = "with v_smp_comp_date as ( select s.id_numeric as sampleid,s.id_text,s.status,to_char(s.date_authorised,'yyyy-mm-dd'),"
                + "to_char(s.date_result_required,'yyyy-mm-dd'), s.is_split, case when s.status='A' then case when s.date_authorised<=s.date_result_required"
                + " then 'T' else 'F' end else case when s.date_result_required>=sysdate then 'T' else 'F' end end wc from sample s where s.login_date>=date'"
                + date
                + "' and trim(s.parent_sample)<>'0' and s.status not in ('X','R') and (s.samptype1='JCYP' or s.samptype1='TSJCYP')), t as (select count(sampleid) "
                + "num from v_smp_comp_date), js as (select count(sampleid) num from v_smp_comp_date where wc='T'), bjs as (select count(sampleid) num from v_smp_comp_date"
                + " where wc='F'), g as (select t.num allcount,nvl(js.num,0) jscount,nvl(bjs.num,0) bjscount from t,js,bjs) select allcount,jscount,bjscount,round(jscount/allcount*100,1) jsl from g";
        try {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sSQLString);
            for (Map<String, Object> map : mapList) {
                list.add(new LookupPassItem("All", "", map.get("jsl").toString().trim(),
                        map.get("allcount").toString().trim(), map.get("jscount").toString().trim(),
                        map.get("bjscount").toString().trim()));
            }
            return list;
        } catch (final Exception e) {
            log.error("【runGetTotalTimeLinessSQL】异常，SQL语句为：{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }

    @Override
    public ArrayList<LookupItem> runGetAliquotListByJobName(String name) {
        ArrayList<String> csxmList = new ArrayList<>();
        ArrayList<LookupItem> aliquotList = new ArrayList<>();
        ArrayList<LookupItem> resultList = new ArrayList<>();
        Boolean flag = true;
        csxmList = runSinglerValueListSQL("csxm_description", "Sample", " where job_name = '" + name + "' and is_split = 'T' and status not in ('X','R')");
        if (csxmList != null) {
            final String sSqlString = "select csxm_description, status from SAMPLE where job_name = '" + name + "' and is_split = 'T' and status not in ('X','R')";
            aliquotList = runGetIDValueSQL(sSqlString, "csxm_description", "status");
        }
        if (aliquotList != null) {
            for (String csxm : csxmList) {
                outer:
                for (LookupItem aliquot : aliquotList) {
                    if (aliquot.getId().equals(csxm)) {
                        if (!aliquot.getValue().equals("A")) {
                            flag = false;
                            break outer;
                        }
                    } 
                }
                if (!flag)
                    resultList.add(new LookupItem(csxm, "未完成"));
                else
                    resultList.add(new LookupItem(csxm, "已完成"));
            }
        }
        return resultList;
    }

    @Override
    public ArrayList<JobInfo> runGetJobListByDate(String FromDate, String ToDate) {
        ArrayList<JobInfo> jobList = new ArrayList<>();
        final String sSqlString = "select job_name, job_status, sqstatus, submitter_oper, authoriser, date_created, date_completed from JOB_HEADER where client_user = '" + operatorName +"' and (date_created >= Date'"
            + FromDate + "') and (date_created <= Date'" + ToDate + "')";
        jobList = runGetJobValueSQL(sSqlString);
        return jobList;

    }

    private ArrayList<JobInfo> runGetJobValueSQL(final String sSQLString) {
        final ArrayList<JobInfo> jobInfoList = new ArrayList<>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sSQLString);
            list.forEach(map -> {
                String dateCreated = "";
                String dateCompleted = "";
                // 时间数据库没有默认值，可能为null
                if (map.get("date_created") != null) {
                    dateCreated = map.get("date_created").toString();
                }
                if (map.get("date_completed") != null) {
                    dateCompleted = map.get("date_completed").toString();
                }

                jobInfoList.add(new JobInfo(map.get("job_name").toString().trim(),
                    LookupItemUtils.findLookupStateItemById(map.get("job_status").toString().trim(), jobStatus), 
                    LookupItemUtils.findLookupStateItemById(map.get("sqstatus").toString().trim(), sqStatus),
                    LookupItemUtils.findLookupItemById(map.get("submitter_oper").toString().trim(), staffs) , 
                    LookupItemUtils.findLookupItemById(map.get("authoriser").toString().trim(), staffs),
                    dateCreated, dateCompleted));
            });
            return jobInfoList;
        } catch (final Exception e) {
            log.error("【runGetJobValueSQL】异常，SQL语句为:{}, 异常信息为:{}", sSQLString, e.getMessage());
            return null;
        }
    }
}