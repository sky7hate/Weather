package com.way.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.way.app.Application;
import com.way.bean.F1;
import com.way.bean.Weatherinfo;
import com.way.util.TimeUtil;
import com.way.weather.R;

import java.util.List;

public class SecondWeatherFragment extends Fragment {
	private TextView weekTv1, weekTv2, weekTv3;
	private ImageView weather_imgIv1, weather_imgIv2, weather_imgIv3;
	private TextView temperatureTv1, temperatureTv2, temperatureTv3;
	private TextView climateTv1, climateTv2, climateTv3;
	private TextView windTv1, windTv2, windTv3;
	private static final String[] wind_state = new String[] {
			"无持续风向", "东北风", "东风", "东南风", "南风", "西南风", "西风", "西北风", "北风", "旋转风"
	};
	private static final String[] weather_state = new String[] {
			"晴","多云","阴","阵雨","雷阵雨","雷阵雨伴有冰雹","雨夹雪","小雨","中雨","大雨","暴雨","大暴雨",
			"特大暴雨","阵雪","小雪","中雪","大雪","暴雪","雾","冻雨","沙尘暴","小到中雨","中到大雨","大到暴雨","暴雨到大暴雨",
			"大暴雨到特大暴雨","小到中雪","中到大雪","大到暴雪","浮尘","扬沙","强沙尘暴","霾","无"
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.biz_plugin_weather_item,
				container, false);
		View view1 = view.findViewById(R.id.subitem1);

		weekTv1 = (TextView) view1.findViewById(R.id.week);
		weekTv1.setText(TimeUtil.getWeek(2, TimeUtil.XING_QI));

		weather_imgIv1 = (ImageView) view1.findViewById(R.id.weather_img);
		temperatureTv1 = (TextView) view1.findViewById(R.id.temperature);

		climateTv1 = (TextView) view1.findViewById(R.id.climate);

		windTv1 = (TextView) view1.findViewById(R.id.wind);
		return view;
	}

	public void updateWeather(List<F1> weatherinfo) {

		if (weatherinfo != null) {

			int climate1 = Integer.parseInt(weatherinfo.get(2).getfa());
			int climate2 = Integer.parseInt(weatherinfo.get(2).getfb());
			if (climate1 > 31) {
				if (climate1 == 53) climate1 = 32;
				else climate1 = 33;
			}
			if (climate2 > 31) {
				if (climate2 == 53) climate2 = 32;
				else climate2 = 33;
			}
			weather_imgIv1.setImageResource(Application.getInstance()
					.getWeatherIcon(weather_state[climate1]));
			if (climate1 != climate2)
				climateTv1.setText(weather_state[climate1] + "转" + weather_state[climate2]);
			else climateTv1.setText(weather_state[climate1]);

			temperatureTv1.setText(weatherinfo.get(2).getfd() + "~" + weatherinfo.get(2).getfc() + "℃");

			int wind_s = Integer.parseInt(weatherinfo.get(2).getfe());
			windTv1.setText(wind_state[wind_s]);
		} else {
			weather_imgIv1.setImageResource(R.drawable.na);

			climateTv1.setText("N/A");

			temperatureTv1.setText("N/A");

			windTv1.setText("N/A");
		}
	}

}
