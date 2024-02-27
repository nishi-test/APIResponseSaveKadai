package org.example.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.JsonValueDto;
import org.example.entity.AiAnalysisLog;
import org.example.service.AiAnalysisLogService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
@Controller
public class ApiController {

    @Autowired
    private AiAnalysisLogService aiAnalysisLogService;


    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 入力エラーリダイレクト用
     */
    @GetMapping("/fail")
    public String showInputErrorForm(@RequestParam("errorMessage") String errorMessage, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("errorMessage", errorMessage);
        return "input.html";
    }

    /**
     * DB保存時エラー用
     */
    @GetMapping("/saveException")
    public String showExceptionMessage(@RequestParam("stackTrace")String stackTrace, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("stackTrace", stackTrace);
        return "exception.html";
    }

    /**
     * DB保存成功用
     */
    @GetMapping("/success")
    public String showInputSuccessForm(@RequestParam("apiResultMessage") String apiResultMessage, RedirectAttributes redirectAttributes) {
                redirectAttributes.addAttribute("apiResultMessage", apiResultMessage);
        return "result.html";
    }

    @Transactional
    @PostMapping("/validateInput")
    public String validateInput(@RequestParam("imagePath") String imagePath, Model model,RedirectAttributes redirectAttributes) throws IOException {
        // バリデーションチェック
        if (isValidInput(imagePath)) {
            String errorMessage = "入力に誤りがあります。入力内容を確認してください";
            model.addAttribute("errorMessage", errorMessage);
            return "redirect:/fail?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }

        //APIへのリクエスト時間
        LocalDateTime requestTime = LocalDateTime.now();
        //mockのAPIに入力値を渡して結果をjsonで取得
        String responseJson = getImagePathClassNameApi(imagePath);
        //APIからのレスポンス時間
        LocalDateTime responseTime = LocalDateTime.now();

        //JSONから値を取得
        JsonValueDto jsonValueDto = getJsonValues(responseJson);

        //Entityに詰める
        AiAnalysisLog aiAnalysisLog = new AiAnalysisLog();
        aiAnalysisLog.setImagePath(imagePath);
        aiAnalysisLog.setSuccess(jsonValueDto.isSuccess());
        aiAnalysisLog.setMessage(jsonValueDto.getMessage());
        aiAnalysisLog.setClassName(jsonValueDto.getClassValue());
        aiAnalysisLog.setConfidence(jsonValueDto.getConfidence());
        aiAnalysisLog.setRequestTimestamp(requestTime);
        aiAnalysisLog.setResponseTimeStamp(responseTime);


        boolean exceptionFlag = false;
        String stackTrace = "";
        try {
            //DBに保存
            aiAnalysisLogService.saveData(aiAnalysisLog);
            if ("/test".equals(imagePath)) {
                //リダイレクト先を制御するため
                exceptionFlag = true;
                // 例外を強制的に投げる
                throw new Exception("This is test exception.");
            }
        } catch (Exception e) {
            // トランザクションをロールバック
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // エラーをログに記録
            log.error("データの保存中にエラーが発生しました: {}", e.getMessage(), e);

            // スタックトレースを文字列に変換してエラーメッセージとしてビューに追加する
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            stackTrace = sw.toString();
            model.addAttribute("stackTrace", stackTrace);
        }

        //リダイレクト画面の制御
        if(exceptionFlag) {
            // 管理者用のエラーページにリダイレクトする
            return "redirect:/saveException?stackTrace=" + URLEncoder.encode(stackTrace, StandardCharsets.UTF_8);
        } else {
            String apiResultMessage = "APIからの取得結果をDBに保存しました";
            // モデルにAPIの結果を追加して画面に渡す
            model.addAttribute("apiResultMessage", apiResultMessage);

            return "redirect:/success?apiResultMessage=" + URLEncoder.encode(apiResultMessage, StandardCharsets.UTF_8);
        }
    }

    /*
     * ヴァリデーションチェック
     */
    public boolean isValidInput(String input) {
        // nullチェック
        if (input == null) {
            return true;
        }

        // 空白チェック
        if (StringUtils.isEmpty(input)) {
            return true;
        }
        // / が含まれているかどうかのチェック
        if (!input.contains("/")) {
            return true;
        }

        return false;
    }

    /*
     * APIのmock-up
     * 課題のサンプルパス（image/d03f1d36ca69348c51aa/c413eac329e1c0d03/test.jpg）の場合のみ成功し、
     * それ以外（ヴァリデーションをパスしたもの）はレスポンスが失敗したパターンになる
     */
    public String getImagePathClassNameApi(String imagePath) throws IOException {
        //画面からの入力でAPIからの取得結果をコントロールするため
        boolean responseFlag = getResponseWhich(imagePath);

        // WireMockサーバーを起動(8080がデフォルトで使われているため)
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8081));
        wireMockServer.start();

        configureFor("localhost", 8081);

        if(responseFlag) {
            // モックAPIエンドポイントの設定(成功する場合)
            stubFor(post(urlEqualTo("http://example.com/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"success\": true, \"message\": \"success\", \"estimated_data\": {\"class\": 3,\"confidence\": 0.8683}}")));


        } else {
            // モックAPIエンドポイントの設定(失敗する場合)
            stubFor(post(urlEqualTo("http://example.com/"))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"success\": false, \"message\": \"error:E50012\", \"estimated_data\": {\"class\": null,\"confidence\": null}}")));
        }

        // リクエストを送信してレスポンスを取得
        String responseBody = wireMockServer.getStubMappings().get(0).getResponse().getBody();

        // WireMockサーバーを停止
        wireMockServer.stop();

        return responseBody;
    }

    public boolean getResponseWhich(String imagePath) {
        if(("/image/d03f1d36ca69348c51aa/c413eac329e1c0d03/test.jpg").equals(imagePath)){
            return true;
        } else {
            return false;
        }
    }

    public JsonValueDto getJsonValues(String responseBody) {
        // レスポンスボディをJSONオブジェクトに変換
        JSONObject jsonResponse = new JSONObject(responseBody);
        JsonValueDto jsonValueDto = new JsonValueDto();
        // jsonオブジェクトから抽出した値をDTOにセット
        jsonValueDto.setSuccess(jsonResponse.getBoolean("success"));
        jsonValueDto.setMessage(jsonResponse.getString("message"));
        JSONObject estimatedData = jsonResponse.getJSONObject("estimated_data");
        //nullチェック
        jsonValueDto.setClassValue(estimatedData.isNull("class") ? null : estimatedData.getLong("class"));
        jsonValueDto.setConfidence(estimatedData.isNull("confidence") ? null : estimatedData.getBigDecimal("confidence"));

        return jsonValueDto;
    }
}
