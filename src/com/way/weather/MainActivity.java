package com.way.weather;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.way.apapter.WeatherPagerAdapter;
import com.way.app.Application;
import com.way.bean.City;
import com.way.bean.F;
import com.way.bean.F1;
import com.way.bean.Pm2d5;
import com.way.bean.SimpleWeather;
import com.way.bean.SimpleWeatherinfo;
import com.way.bean.Weather;
import com.way.bean.Weatherinfo;
import com.way.db.CityDB;
import com.way.fragment.FirstWeatherFragment;
import com.way.fragment.SecondWeatherFragment;
import com.way.indicator.CirclePageIndicator;
import com.way.util.ConfigCache;
import com.way.util.IphoneDialog;
import com.way.util.L;
import com.way.util.NetUtil;
import com.way.util.SharePreferenceUtil;
import com.way.util.T;
import com.way.util.TimeUtil;

import javax.crypto.Mac;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.TimeZone;

import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends FragmentActivity implements
		Application.EventHandler, OnClickListener {
	public static final String UPDATE_WIDGET_WEATHER_ACTION = "com.way.action.update_weather";
	public static final String WEATHER_SIMPLE_URL = "http://www.weather.com.cn/data/sk/";// 简要天气信息
	public static final String WEATHER_BASE_URL = "http://m.weather.com.cn/data/";// 详细天气
	public static final String WEATHER_URL = "http://open.weather.com.cn/data/";
	public static final String PM2D5_BASE_URL = "http://www.pm25.in/api/querys/pm2_5.json?city=SHENZHEN&token=HUL7sQBaUKVvgWzdKdTB&stations=no";
	private static final String WEATHER_INFO_FILENAME = "_weather.json";
	private static final String SIMPLE_WEATHER_INFO_FILENAME = "_simple_weather.json";
	private static final String PM2D5_INFO_FILENAME = "_pm2d5.json";
	private static final String AREAID = "?areaid=";
	private static final String TYPEurl = "&type=";
	private static final String DATEurl = "&date=";
	private static final String APPIDurl = "&appid=";
	private static final String KEYurl = "&key=";
	private static final String TYPE = "forecast_v";
	private static final String SimpleTYPE = "forecast_f";
	private static final String APPID = "fa1d6c8e1b3ed124";
	private static final String KEY = "d7e9a6_SmartWeatherAPI_fcbaa08";
	private static final int LOCATION_OK = 0;
	private static final int ON_NEW_INTENT = 1;
	private static final int UPDATE_EXISTS_CITY = 2;
	private static final int GET_WEATHER_RESULT = 3;
    public static final HashMap<String, Integer> ProvinceID = new HashMap<String, Integer>();
    private LocationClient mLocationClient;
	private CityDB mCityDB;
	private SharePreferenceUtil mSpUtil;
	private Application mApplication;
	private City mCurCity;
	private List<F1> mCurWeatherinfo;
	private String Time_stamp;
	private SimpleWeatherinfo mCurSimpleWeatherinfo;
	private Pm2d5 mCurPm2d5;
	private Gson mGson;
	private ImageView mCityManagerBtn, mUpdateBtn, mLocationBtn, mShareBtn;
	private ProgressBar mUpdateProgressBar;
	private TextView mTitleTextView;
	private City mNewIntentCity;
	private WeatherPagerAdapter mWeatherPagerAdapter;
	private static final char last2byte = (char) Integer.parseInt("00000011", 2);
	private static final char last4byte = (char) Integer.parseInt("00001111", 2);
	private static final char last6byte = (char) Integer.parseInt("00111111", 2);
	private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
	private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
	private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
	private static final char[] encodeTable = new char[] { 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '+', '/'
	};
	private static final String[] wind_state = new String[] {
			"无持续风向", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风", "北风", "旋转风"
	};
	private static final String[] wind_power = new String[] {
			"微风","3-4级","4-5级","5-6级","6-7级","7-8级","8-9级","9-10级","10-11级","11-12级"
	};
	private static final String[] weather_state = new String[] {
			"晴","多云","阴","阵雨","雷阵雨","雷阵雨伴有冰雹","雨夹雪","小雨","中雨","大雨","暴雨","大暴雨",
			"特大暴雨","阵雪","小雪","中雪","大雪","暴雪","雾","冻雨","沙尘暴","小到中雨","中到大雨","大到暴雨","暴雨到大暴雨",
			"大暴雨到特大暴雨","小到中雪","中到大雪","大到暴雪","浮尘","扬沙","强沙尘暴","霾","无"
	};


    private LinearLayout main_part;
	private TextView cityTv, timeTv, humidityTv, weekTv, SunriseTv, SunsetTv,
			temperatureTv, climateTv, windTv;
	private ImageView weatherImg, pmImg;;
	private ViewPager mViewPager;
	private List<Fragment> fragments;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOCATION_OK:

				String cityName = (String) msg.obj;
				L.i("cityName = " + cityName);
				mCurCity = mCityDB.getCity(cityName);
				L.i(mCurCity.toString());
				mSpUtil.setCity(mCurCity.getCity());
				cityTv.setText(mCurCity.getCity());
				changebackgroud(mCurCity);
				updateWeather(true);
				break;
			case ON_NEW_INTENT:
				mCurCity = mNewIntentCity;
				mSpUtil.setCity(mCurCity.getCity());
				cityTv.setText(mCurCity.getCity());
				changebackgroud(mCurCity);
				updateWeather(true);
				break;
			case UPDATE_EXISTS_CITY:
				String sPCityName = mSpUtil.getCity();
				mCurCity = mCityDB.getCity(sPCityName);
				changebackgroud(mCurCity);
				updateWeather(false);
				break;
			case GET_WEATHER_RESULT:
				updateWeatherInfo();
				updatePm2d5Info();
				updateWidgetWeather();
				mUpdateBtn.setVisibility(View.VISIBLE);
				mUpdateProgressBar.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}

	};

	private void updateWidgetWeather() {
		sendBroadcast(new Intent(UPDATE_WIDGET_WEATHER_ACTION));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}

	private void startActivityForResult() {
		Intent i = new Intent(this, SelectCtiyActivity.class);
		startActivityForResult(i, 0);
	}

	private void initView() {
        main_part = (LinearLayout) findViewById(R.id.main_part);
		mCityManagerBtn = (ImageView) findViewById(R.id.title_city_manager);
		mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
		mShareBtn = (ImageView) findViewById(R.id.title_share);
		mLocationBtn = (ImageView) findViewById(R.id.title_location);
		mCityManagerBtn.setOnClickListener(this);
		mUpdateBtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mLocationBtn.setOnClickListener(this);
		mShareBtn.setVisibility(View.GONE);
		mUpdateProgressBar = (ProgressBar) findViewById(R.id.title_update_progress);
		mTitleTextView = (TextView) findViewById(R.id.title_city_name);

		cityTv = (TextView) findViewById(R.id.city);
		timeTv = (TextView) findViewById(R.id.time);
		timeTv.setText(TimeUtil.getDay(mSpUtil.getTimeSamp())
				+ mSpUtil.getTime() + "发布");
		humidityTv = (TextView) findViewById(R.id.humidity);
		weekTv = (TextView) findViewById(R.id.week_today);
		weekTv.setText("今天 " + TimeUtil.getWeek(0, TimeUtil.XING_QI));
		SunriseTv = (TextView) findViewById(R.id.sunrise);
		SunsetTv = (TextView) findViewById(R.id.sunset);
		temperatureTv = (TextView) findViewById(R.id.temperature);
		climateTv = (TextView) findViewById(R.id.climate);
		windTv = (TextView) findViewById(R.id.wind);
		weatherImg = (ImageView) findViewById(R.id.weather_img);
		fragments = new ArrayList<Fragment>();
		fragments.add(new FirstWeatherFragment());
		fragments.add(new SecondWeatherFragment());
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mWeatherPagerAdapter = new WeatherPagerAdapter(
				getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(mWeatherPagerAdapter);
		((CirclePageIndicator) findViewById(R.id.indicator))
				.setViewPager(mViewPager);
		if (TextUtils.isEmpty(mSpUtil.getCity())) {
			if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
				mLocationClient.start();
				mLocationClient.requestLocation();
				T.showShort(this, "正在定位...");
				mUpdateBtn.setVisibility(View.GONE);
				mUpdateProgressBar.setVisibility(View.VISIBLE);
			} else {
				T.showShort(this, R.string.net_err);
			}
		} else {
			mHandler.sendEmptyMessage(UPDATE_EXISTS_CITY);
		}
	}

	private void initData() {
		Application.mListeners.add(this);
		mApplication = Application.getInstance();
		mSpUtil = mApplication.getSharePreferenceUtil();
		mLocationClient = mApplication.getLocationClient();

		mLocationClient.registerLocationListener(mLocationListener);
		mCityDB = mApplication.getCityDB();
		// 不转换没有 @Expose 注解的字段
		mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
        ProvinceID.put("重庆",0);
		ProvinceID.put("北京",1);
		ProvinceID.put("天津",2);
		ProvinceID.put("上海",3);
		ProvinceID.put("河北",4);
		ProvinceID.put("河南",5);
		ProvinceID.put("湖北",6);
		ProvinceID.put("湖南", 7);
		ProvinceID.put("江苏", 8);
		ProvinceID.put("江西", 9);
		ProvinceID.put("辽宁", 10);
		ProvinceID.put("吉林", 11);
		ProvinceID.put("黑龙江",12);
		ProvinceID.put("陕西",13);
		ProvinceID.put("山西",14);
		ProvinceID.put("山东",15);
		ProvinceID.put("四川", 16);
		ProvinceID.put("青海",17);
		ProvinceID.put("海南",18);
		ProvinceID.put("安徽", 19);
		ProvinceID.put("广东",20);
		ProvinceID.put("贵州",21);
		ProvinceID.put("浙江",22);
		ProvinceID.put("福建",23);
		ProvinceID.put("台湾",24);
		ProvinceID.put("甘肃",25);
		ProvinceID.put("云南",26);
		ProvinceID.put("西藏",27);
		ProvinceID.put("宁夏", 28);
		ProvinceID.put("广西", 29);
		ProvinceID.put("新疆",30);
		ProvinceID.put("内蒙古",31);
		ProvinceID.put("香港", 32);
		ProvinceID.put("澳门", 33);

	}
	public static String standardURLEncoder(String data, String key) {
		byte[] byteHMAC = null;
		String urlEncoder = "";
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			mac.init(spec);
			byteHMAC = mac.doFinal(data.getBytes());
			if (byteHMAC != null) {
				String oauth = encode(byteHMAC);
				if (oauth != null) {
					urlEncoder = URLEncoder.encode(oauth, "utf8");
				}
			}
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return urlEncoder;
	}

	public static String encode(byte[] from) {
		StringBuffer to = new StringBuffer((int) (from.length * 1.34) + 3);
		int num = 0;
		char currentByte = 0;
		for (int i = 0; i < from.length; i++) {
			num = num % 8;
			while (num < 8) {
				switch (num) {
					case 0:
						currentByte = (char) (from[i] & lead6byte);
						currentByte = (char) (currentByte >>> 2);
						break;
					case 2:
						currentByte = (char) (from[i] & last6byte);
						break;
					case 4:
						currentByte = (char) (from[i] & last4byte);
						currentByte = (char) (currentByte << 2);
						if ((i + 1) < from.length) {
							currentByte |= (from[i + 1] & lead2byte) >>> 6;
						}
						break;
					case 6:
						currentByte = (char) (from[i] & last2byte);
						currentByte = (char) (currentByte << 4);
						if ((i + 1) < from.length) {
							currentByte |= (from[i + 1] & lead4byte) >>> 4;
						}
						break;
				}
				to.append(encodeTable[currentByte]);
				num += 6;
			}
		}
		if (to.length() % 4 != 0) {
			for (int i = 4 - to.length() % 4; i > 0; i--) {
				to.append("=");
			}
		}
		return to.toString();
	}

	private void changebackgroud(City city) {
        String curPro = city.getProvince();
        int startview = R.drawable.v00;
        int Pid = ProvinceID.get(curPro), count = 0;
        while (count < Pid) {
            startview++;
            count++;
        }
        main_part.setBackgroundResource(startview);
	}

	private void updateWeather(final boolean isRefresh) {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE && isRefresh) {
			T.showLong(this, R.string.net_err);
			return;
		}
		if (mCurCity == null) {
			T.showLong(mApplication, "未找到此城市,请重新定位或选择...");
			return;
		}
		// T.showShort(this, "正在刷新天气...");
		timeTv.setText("同步中...");
		mTitleTextView.setText(mCurCity.getCity() + "天气");
		mUpdateBtn.setVisibility(View.GONE);
		mUpdateProgressBar.setVisibility(View.VISIBLE);
		// 启动线程获取天气信息
		new Thread() {
			@Override
			public void run() {
				super.run();
				getWeatherInfo(isRefresh);
				//getSimpleWeatherInfo(isRefresh);
				getPm2d5Info(isRefresh);

				if (mCurSimpleWeatherinfo != null)
					L.i(mCurSimpleWeatherinfo.toString());
				if (mCurWeatherinfo != null)
					L.i(mCurWeatherinfo.toString());
				if (mCurPm2d5 != null)
					L.i(mCurPm2d5.toString());
				mHandler.sendEmptyMessage(GET_WEATHER_RESULT);
			}

		}.start();
	}

	private String getTime() {
		TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
		TimeZone.setDefault(tz);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String time = df.format(new Date()).toString();
		return time;
	}


	private void getWeatherInfo(boolean isRefresh) {
		String base = WEATHER_URL + AREAID + mCurCity.getNumber() + TYPEurl + TYPE + DATEurl + getTime();
		String data = base + APPIDurl + APPID;
		String encrypt = standardURLEncoder(data, KEY);
		String url = base + APPIDurl + APPID.substring(0,6) + KEYurl + encrypt;
		String result;
		if (!isRefresh) {
			if (mApplication.getmCurWeatherinfo() != null) {// 读取内存中的信息
				mCurWeatherinfo = mApplication.getmCurWeatherinfo();
				L.i("get the weather info from memory");
				return;// 直接返回，不继续执行
			}
			// result = getInfoFromFile(WEATHER_INFO_FILENAME);// 文件中的信息
			result = ConfigCache.getUrlCache(url);
			if (!TextUtils.isEmpty(result)) {
				parseWeatherInfo(url, result, false);
				L.i("get the weather info from file");
				return;
			}
		}

		 L.i("weather url: " + url);
		String weatherResult = connServerForResult(url);
		//L.i(weatherResult);
		if (TextUtils.isEmpty(weatherResult)) {
			weatherResult = getInfoFromFile(WEATHER_INFO_FILENAME);
			L.i("SB");
		}
		L.i(weatherResult);
		parseWeatherInfo(url, weatherResult, true);
	}

	private void getSimpleWeatherInfo(boolean isRefresh) {
		String url = WEATHER_SIMPLE_URL + mCurCity.getNumber() + ".html";
		String result;
		if (!isRefresh) {
			if (mApplication.getCurSimpleWeatherinfo() != null) {// 读取内存中的信息
				mCurSimpleWeatherinfo = mApplication.getCurSimpleWeatherinfo();
				L.i("get the simple weather info from memory");
				return;// 直接返回，不继续执行
			}
			// result = getInfoFromFile(SIMPLE_WEATHER_INFO_FILENAME);// 文件中的信息
			result = ConfigCache.getUrlCache(url);
			if (!TextUtils.isEmpty(result)) {
				parseSimpleWeatherInfo(url, result, false);
				L.i("get the simple weather info from file");
				return;
			}
		}

		// L.i("weather url: " + url);
		String weatherResult = connServerForResult(url);
		if (TextUtils.isEmpty(weatherResult))
			weatherResult = getInfoFromFile(SIMPLE_WEATHER_INFO_FILENAME);
		parseSimpleWeatherInfo(url, weatherResult, true);
	}

	private void getPm2d5Info(boolean isRefresh) {
		String urlPm2d5 = PM2D5_BASE_URL.replace("SHENZHEN",
				mCurCity.getAllPY());
		String result;
		if (!isRefresh) {
			if (mApplication.getmCurPm2d5() != null) {// 内存中的信息
				mCurPm2d5 = mApplication.getmCurPm2d5();
				L.i("get the pm2.5 info from memory");
				return;
			}
			// result = getInfoFromFile(PM2D5_INFO_FILENAME);// 文件中的信息
			result = ConfigCache.getUrlCache(urlPm2d5);
			if (!TextUtils.isEmpty(result)) {
				parsePm2d5Info(urlPm2d5, result, false);
				L.i("get the pm2.5 info from file");
				return;
			}
		}
		// L.i("pm2.5 url: " + urlPm2d5);
		String pmResult = connServerForResult(urlPm2d5);
		if (TextUtils.isEmpty(pmResult) || pmResult.contains("error")) {// 如果获取失败，则取本地文件中的信息，
			String fileResult = getInfoFromFile(PM2D5_INFO_FILENAME);
			// 只有当本地文件信息与当前城市相匹配时才使用
			if (!TextUtils.isEmpty(fileResult)
					&& fileResult.contains(mCurCity.getCity()))
				pmResult = fileResult;
		}
		// pmResult = getInfoFromFile(PM2D5_INFO_FILENAME);

		parsePm2d5Info(urlPm2d5, pmResult, true);
	}

	private void parseWeatherInfo(String url, String result,
			boolean isRefreshWeather) {
		mCurWeatherinfo = null;
		mApplication.setmCurWeatherinfo(null);
		int index0 = result.indexOf("\"f1\"");
		int index1 = result.indexOf("\"f0\"");
		//System.out.println(index0);
		String tt = result.substring(index0 + 5, index1 - 1);
		Time_stamp = result.substring(index1+6, result.length()-3);
		if (!TextUtils.isEmpty(result) && !result.contains("页面没有找到")) {
			// L.i(result);
			List<F1> f = new ArrayList<F1>();
			f = mGson.fromJson(tt, new TypeToken<List<F1>>(){}.getType());
			mCurWeatherinfo = f;
			 L.i(mCurWeatherinfo.get(0).toString());
			mApplication.setmCurWeatherinfo(mCurWeatherinfo);
		} else {
			result = "";
		}
		if (isRefreshWeather && !TextUtils.isEmpty(result)) {
			 save2File(result, WEATHER_INFO_FILENAME);
			ConfigCache.setUrlCache(result, url);}
	}

	private void parseSimpleWeatherInfo(String url, String result,
			boolean isRefreshWeather) {
		mCurSimpleWeatherinfo = null;
		mApplication.setCurSimpleWeatherinfo(null);
		if (!TextUtils.isEmpty(result) && !result.contains("页面没有找到")) {
			// L.i(result);
			SimpleWeather weather = mGson.fromJson(result, SimpleWeather.class);
			mCurSimpleWeatherinfo = weather.getWeatherinfo();
			// L.i(mCurSimpleWeatherinfo.toString());
			mApplication.setCurSimpleWeatherinfo(mCurSimpleWeatherinfo);
		} else {
			result = "";
		}
		if (isRefreshWeather && !TextUtils.isEmpty(result)) {
			 save2File(result, SIMPLE_WEATHER_INFO_FILENAME);
			ConfigCache.setUrlCache(result, url);}
	}

	private void parsePm2d5Info(String url, String result,
			boolean isRefreshPm2d5) {
		mCurPm2d5 = null;
		mApplication.setmCurWeatherinfo(null);
		if (!TextUtils.isEmpty(result) && !result.contains("error")) {
			// L.i(result);
			List<Pm2d5> pm2d5s = mGson.fromJson(result,
					new TypeToken<List<Pm2d5>>() {
					}.getType());
			mCurPm2d5 = pm2d5s.get(0);
			// L.i(mCurPm2d5.toString());
		} else {
			result = "";
		}
		if (isRefreshPm2d5 && !TextUtils.isEmpty(result)) {
			 save2File(result, PM2D5_INFO_FILENAME);
			ConfigCache.setUrlCache(result, url);}
	}

	// 把信息保存到文件中
	private boolean save2File(String result, String fileName) {
		try {
			FileOutputStream fos = MainActivity.this.openFileOutput(fileName,
					MODE_PRIVATE);
			fos.write(result.toString().getBytes());
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 从文件中获取信息
	 * 
	 * @param fileName
	 * @return
	 */
	private String getInfoFromFile(String fileName) {
		String result = "";
		try {
			FileInputStream fis = openFileInput(fileName);
			byte[] buffer = new byte[fis.available()];// 本地文件可以实例化buffer，网络文件不可行
			fis.read(buffer);
			result = new String(buffer);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private int judgeTime(String time) {
		int hour = 0;
		hour = Integer.parseInt(time.substring(8, 10));
		return hour;
	}

	/**
	 * 更新天气界面
	 */
	private void updateWeatherInfo() {
		if (mCurWeatherinfo != null) {
			mApplication.setmCurWeatherinfo(mCurWeatherinfo);// 保存到全局变量中

			int wind_p;
			timeTv.setText(Time_stamp + "发布");
			L.i(getTime());
			if (judgeTime(getTime()) < 10) {
				wind_p = Integer.parseInt(mCurWeatherinfo.get(0).getfg());
			}
			else {
				wind_p = Integer.parseInt(mCurWeatherinfo.get(0).getfh());
			}
			humidityTv.setText("风力：" + wind_power[wind_p]);

			if (judgeTime(getTime()) < 10)
				temperatureTv.setText(mCurWeatherinfo.get(0).getfd() + "~" + mCurWeatherinfo.get(0).getfc() + "℃");
			else temperatureTv.setText(mCurWeatherinfo.get(0).getfd());
			cityTv.setText(mCurCity.getCity());
			int wind;
			if (judgeTime(getTime()) < 10)
				wind = Integer.parseInt(mCurWeatherinfo.get(0).getfe());
			else wind = Integer.parseInt(mCurWeatherinfo.get(0).getff());

			windTv.setText(wind_state[wind]);
			SunriseTv.setText(mCurWeatherinfo.get(0).getfi().substring(0,5));
			SunsetTv.setText(mCurWeatherinfo.get(0).getfi().substring(6,11));
			int climate2 = Integer.parseInt(mCurWeatherinfo.get(0).getfb());
			int climate1 = 0;
			if (judgeTime(getTime()) < 10) climate1 = Integer.parseInt(mCurWeatherinfo.get(0).getfa());
			if (climate1 > 31) {
				if (climate1 == 53) climate1 = 32;
				else climate1 = 33;
			}
			if (climate2 > 31) {
				if (climate2 == 53) climate2 = 32;
				else climate2 = 33;
			}
			if (climate1 != climate2 && judgeTime(getTime()) < 10)
				climateTv.setText(weather_state[climate1] + "转" + weather_state[climate2]);
			else climateTv.setText(weather_state[climate2]);
			/*mSpUtil.setSimpleClimate(climate);
			String[] strs = { "晴", "晴" };
			if (climate.contains("转")) {// 天气带转字，取前面那部分
				strs = climate.split("转");
				climate = strs[0];
				if (climate.contains("到")) {// 如果转字前面那部分带到字，则取它的后部分
					strs = climate.split("到");
					climate = strs[1];
				}
			}
			L.i("处理后的天气为：" + climate);*/
			int curclimate = climate1;
			if (judgeTime(getTime()) > 9) curclimate = climate2;

			if (mApplication.getWeatherIconMap().containsKey(weather_state[curclimate])) {
				int iconRes = mApplication.getWeatherIconMap().get(weather_state[curclimate]);
				weatherImg.setImageResource(iconRes);
			} else {
				// do nothing 没有这样的天气图片

			}}
			/*else
			if (mCurSimpleWeatherinfo != null) {
				if (!mCurSimpleWeatherinfo.getTime().equals(mSpUtil.getTime())) {
					mSpUtil.setTime(mCurSimpleWeatherinfo.getTime());
					mSpUtil.setTimeSamp(System.currentTimeMillis());// 保存一下更新的时间戳
				}

				cityTv.setText(mCurCity.getCity());
				mSpUtil.setSimpleTemp(mCurSimpleWeatherinfo.getTemp());
				temperatureTv.setText(mCurSimpleWeatherinfo.getTemp());
				timeTv.setText(TimeUtil.getDay(mSpUtil.getTimeSamp())
						+ mCurSimpleWeatherinfo.getTime() + "发布");
				humidityTv.setText("湿度:" + mCurSimpleWeatherinfo.getSD());
				windTv.setText(mCurSimpleWeatherinfo.getWD());
			}*/
		 else {
			temperatureTv.setText("N/A");
			cityTv.setText(mCurCity.getCity());
			windTv.setText("N/A");
			climateTv.setText("N/A");
			weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
			T.showLong(mApplication, "获取天气信息失败");
		}
		if (fragments.size() > 0) {
			((FirstWeatherFragment) mWeatherPagerAdapter.getItem(0))
					.updateWeather(mCurWeatherinfo);
			((SecondWeatherFragment) mWeatherPagerAdapter.getItem(1))
					.updateWeather(mCurWeatherinfo);
		}
	}

	/**
	 * 更新pm2.5界面
	 */
	private void updatePm2d5Info() {
		// do nothing
		/*if (mCurPm2d5 != null) {
			mApplication.setmCurPm2d5(mCurPm2d5);
			pmQualityTv.setText(mCurPm2d5.getQuality());
			pmDataTv.setText(mCurPm2d5.getPm2_5_24h());
			int pm2_5 = Integer.parseInt(mCurPm2d5.getPm2_5_24h());
			int pm_img = R.drawable.biz_plugin_weather_0_50;
			if (pm2_5 > 300) {
				pm_img = R.drawable.biz_plugin_weather_greater_300;
			} else if (pm2_5 > 200) {
				pm_img = R.drawable.biz_plugin_weather_201_300;
			} else if (pm2_5 > 150) {
				pm_img = R.drawable.biz_plugin_weather_151_200;
			} else if (pm2_5 > 100) {
				pm_img = R.drawable.biz_plugin_weather_101_150;
			} else if (pm2_5 > 50) {
				pm_img = R.drawable.biz_plugin_weather_51_100;
			} else {
				pm_img = R.drawable.biz_plugin_weather_0_50;
			}

			pmImg.setImageResource(pm_img);
		} else {
			pmQualityTv.setText("N/A");
			pmDataTv.setText("N/A");
			pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
			T.showLong(mApplication, "未获取到PM2.5数据");
		}*/
	}

	// 请求服务器，获取返回数据
	private String connServerForResult(String url) {
		HttpGet httpRequest = new HttpGet(url);
		String strResult = "";
		if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
			try {
				// HttpClient对象
				HttpClient httpClient = new DefaultHttpClient();
				// 获得HttpResponse对象
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					// 取得返回的数据
					strResult = EntityUtils.toString(httpResponse.getEntity());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		L.i(strResult);
		return strResult; // 返回结果
	}

	BDLocationListener mLocationListener = new BDLocationListener() {

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// do nothing
		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			// mActionBar.setProgressBarVisibility(View.GONE);
			mUpdateBtn.setVisibility(View.VISIBLE);
			if (location == null || TextUtils.isEmpty(location.getCity())) {
				// T.showShort(getApplicationContext(), "location = null");
				if (location == null) L.i("location null");
				else {
					L.i("location no city");
				}
				final Dialog dialog = IphoneDialog.getTwoBtnDialog(
						MainActivity.this, "定位失败", "是否手动选择城市?");
				((Button) dialog.findViewById(R.id.ok))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								startActivityForResult();
								dialog.dismiss();
							}
						});
				dialog.show();
				return;
			}
			String cityName = location.getCity();
			mLocationClient.stop();
			Message msg = mHandler.obtainMessage();
			msg.what = LOCATION_OK;
			msg.obj = cityName;
			mHandler.sendMessage(msg);// 更新天气
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == RESULT_OK) {
			mNewIntentCity = (City) data.getSerializableExtra("city");
			mHandler.sendEmptyMessage(ON_NEW_INTENT);
		}
	}

	@Override
	public void onCityComplete() {
		// do nothing
	}

	@Override
	public void onNetChange() {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE)
			T.showLong(this, R.string.net_err);
		// else if (!TextUtils.isEmpty(mSpUtil.getCity())) {
		// String sPCityName = mSpUtil.getCity();
		// mCurCity = mCityDB.getCity(sPCityName);
		// getWeatherInfo(true, true);
		// }
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_city_manager:
			startActivityForResult();
			break;
		case R.id.title_location:
			if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
				if (!mLocationClient.isStarted())
					mLocationClient.start();
				mLocationClient.requestLocation();
				T.showShort(this, "正在定位...");
			} else {
				T.showShort(this, R.string.net_err);
			}
			break;
		case R.id.title_share:
			if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
				// do something
			} else {
				T.showShort(this, R.string.net_err);
			}
			break;
		case R.id.title_update_btn:
			if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
				if (TextUtils.isEmpty(mSpUtil.getCity())) {
					T.showShort(this, "请先选择城市或定位！");
				} else {
					String sPCityName = mSpUtil.getCity();
					mCurCity = mCityDB.getCity(sPCityName);
					updateWeather(true);
				}
			} else {
				T.showShort(this, R.string.net_err);
			}
			break;

		default:
			break;
		}
	}

}
