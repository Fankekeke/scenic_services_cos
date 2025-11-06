package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

/**
 * 天气查询接口
 *
 * @author FanK
 */
@RestController
@RequestMapping("/cos/weather-info")
public class WeatherController {

    /**
     * 根据城市查询天气信息
     *
     * @param city 城市
     * @return 结果
     */
    @RequestMapping("/queryWeatherByCity")
    public R queryWeatherByCity(String city) throws Exception {
        if (city == null || city.trim().isEmpty()) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("city/city.json");
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode cityCodesArray = rootNode.get("城市代码");
            if (cityCodesArray != null && cityCodesArray.isArray()) {
                // 3. 遍历省份/直辖市
                for (JsonNode provinceNode : cityCodesArray) {
                    JsonNode cityArray = provinceNode.get("市");
                    if (cityArray != null && cityArray.isArray()) {
                        // 4. 遍历城市列表
                        for (JsonNode cityNode : cityArray) {
                            String name = cityNode.get("市名").asText();
                            String code = cityNode.get("编码").asText();
                            if (city.equals(name)) {
                                String url = "http://t.weather.itboy.net/api/weather/city/" + code;
                                String forecast = HttpUtil.get(url);
                                return R.ok(JSONUtil.parse(forecast));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 处理 JSON 解析错误
            e.printStackTrace();
            throw new FebsException("JSON 数据解析失败。");
        }
        // 遍历完所有城市后未找到
        throw new FebsException("未找到该城市的天气信息。");
    }
}
