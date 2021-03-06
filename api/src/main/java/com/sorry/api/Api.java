package com.sorry.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.baidu.location.LocationClient;
import com.sorry.model.PersonalData;
import com.sorry.model.PostData;

import cn.smssdk.EventHandler;
import okhttp3.Callback;
public interface Api {

    String REGISTER = "user.registerByPhone";
    String LOGIN = "user.loginByApp";
    String GET_PINFO = "user.getPersonalInfo";
    String PUSH_PINFO = "user.pushPersonalInfo";
    String PUSH_POST = "user.pushPost";


    public void showToast(Context context, String msg, int aloneTime);

    public void sendSmsCode4Register(String phoneNum);

    /**
     * 核对验证码
     *
     * @param phoneNum 手机号码
     * @param code     验证码
     * @param eh       回调处理
     */
    public void verifySMSCode(Context context, String phoneNum, String code, EventHandler eh);

    /**
     * 登录
     *
     * @param phoneNum 手机号码
     * @param password 密码
     * @param callback 回调处理
     */
    public void registerByPhone(String phoneNum, String password, Callback callback);

    /**
     * 登录
     *
     * @param loginName 登录名（手机号）
     * @param password  MD5加密的密码
     */
    public void loginByApp(String loginName, String password, Callback callback);

    /**
     * 保存用户名密码
     *
     * @param username 用户名（手机号）
     * @param password  明文密码
     */

    public void saveUser(String username, String password);

    /**
     * 获取保存的用户名密码
     * @return
     */

    public Cursor getUser();
    /**
     * 初始化定位
     *
     * @param locationClient 定位客户端
     */

    public void initLocation(LocationClient locationClient);

    /**
     * 得到天气
     *
     * @param latitude 经度
     * @param longitude 纬度
     */
    public void getWeather(String latitude, String longitude, Callback callback);

    /**
     * 获取个人信息
     *
     */
    public void getPersonalInfo(String loginName, String password, Callback callback);
    /**
     * 提交个人信息
     *
     */
    public void pushPersonalInfo(String account, PersonalData personalData, Callback callback);
    /**
     * 查找数据库每天的数据
     *
     */
    public Cursor selectAlldayData();

    /**
     * 插入数据库每天的数据
     *
     */
    public void insertIntoAlldayData(ContentValues contentValues);
    /**
     * 插入数据库当前步数
     *
     */
    public void insertIntoStepData(String step);

    /**
     * 插入数据库当前心跳
     * @param heartRate 心率
     */
    public void insertIntoHeartRateData(String heartRate);

    /**
     * 汇总一天的数据到总表
     */
    public void collectOneDayData();
    public void deleteAlldata(String table);
    public void exectest(String sql);
    public void putPost(PostData postData, Callback callback);

}
