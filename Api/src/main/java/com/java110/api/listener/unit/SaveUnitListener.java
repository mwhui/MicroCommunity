package com.java110.api.listener.unit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.unit.IUnitBMO;
import com.java110.api.listener.AbstractServiceApiDataFlowListener;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.smo.floor.IFloorInnerServiceSMO;
import com.java110.dto.FloorDto;
import com.java110.entity.center.AppService;
import com.java110.event.service.api.ServiceDataFlowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * @ClassName SaveUnitListener
 * @Description TODO 保存小区单元信息
 * @Author wuxw
 * @Date 2019/5/3 11:54
 * @Version 1.0
 * add by wuxw 2019/5/3
 **/
@Java110Listener("saveUnitListener")
public class SaveUnitListener extends AbstractServiceApiDataFlowListener {
    private static Logger logger = LoggerFactory.getLogger(SaveUnitListener.class);
    @Autowired
    private IUnitBMO unitBMOImpl;

    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_SAVE_UNIT;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public void soService(ServiceDataFlowEvent event) {

        logger.debug("ServiceDataFlowEvent : {}", event);

        DataFlowContext dataFlowContext = event.getDataFlowContext();
        AppService service = event.getAppService();

        String paramIn = dataFlowContext.getReqData();

        //校验数据
        validate(paramIn);
        JSONObject paramObj = JSONObject.parseObject(paramIn);

        HttpHeaders header = new HttpHeaders();
        //dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_USER_ID, "-1");
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();

        //添加单元信息
        businesses.add(addUnit(paramObj, dataFlowContext));



        ResponseEntity<String> responseEntity = unitBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);

        dataFlowContext.setResponseEntity(responseEntity);

    }

    /**
     * 添加小区楼信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
    private JSONObject addUnit(JSONObject paramInJson, DataFlowContext dataFlowContext) {


        JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
        business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_SAVE_UNIT_INFO);
        business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ);
        business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
        JSONObject businessUnit = new JSONObject();
        businessUnit.put("floorId", paramInJson.getString("floorId"));
        businessUnit.put("layerCount", paramInJson.getString("layerCount"));
        businessUnit.put("unitId", "-1");
        businessUnit.put("unitNum", paramInJson.getString("unitNum"));
        businessUnit.put("lift", paramInJson.getString("lift"));
        businessUnit.put("remark", paramInJson.getString("remark"));
        businessUnit.put("userId", dataFlowContext.getRequestCurrentHeaders().get(CommonConstant.HTTP_USER_ID));
        business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put("businessUnit", businessUnit);

        return business;
    }

    /**
     * 数据校验
     *
     * @param paramIn "communityId": "7020181217000001",
     *                "memberId": "3456789",
     *                "memberTypeCd": "390001200001"
     */
    private void validate(String paramIn) {
        Assert.jsonObjectHaveKey(paramIn, "communityId", "请求报文中未包含communityId节点");
        Assert.jsonObjectHaveKey(paramIn, "floorId", "请求报文中未包含floorId节点");
        Assert.jsonObjectHaveKey(paramIn, "unitNum", "请求报文中未包含unitNum节点");
        Assert.jsonObjectHaveKey(paramIn, "layerCount", "请求报文中未包含layerCount节点");
        Assert.jsonObjectHaveKey(paramIn, "lift", "请求报文中未包含lift节点");
        JSONObject reqJson = JSONObject.parseObject(paramIn);
        Assert.isInteger(reqJson.getString("layerCount"), "单元总层数据不是有效数字");

        if (!"1010".equals(reqJson.getString("lift")) && !"2020".equals(reqJson.getString("lift"))) {
            throw new IllegalArgumentException("是否有电梯 传入数据错误");
        }

        FloorDto floorDto = new FloorDto();
        floorDto.setCommunityId(reqJson.getString("communityId"));
        floorDto.setFloorId(reqJson.getString("floorId"));
        //校验小区楼ID和小区是否有对应关系
        int total = floorInnerServiceSMOImpl.queryFloorsCount(floorDto);

        if (total < 1) {
            throw new IllegalArgumentException("传入小区楼ID不是该小区的楼");
        }

    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IFloorInnerServiceSMO getFloorInnerServiceSMOImpl() {
        return floorInnerServiceSMOImpl;
    }

    public void setFloorInnerServiceSMOImpl(IFloorInnerServiceSMO floorInnerServiceSMOImpl) {
        this.floorInnerServiceSMOImpl = floorInnerServiceSMOImpl;
    }
}
