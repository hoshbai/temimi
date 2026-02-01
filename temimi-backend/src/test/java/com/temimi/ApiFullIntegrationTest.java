package com.temimi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * 完整的 API 集成测试类
 * 自动测试所有后端接口，包括公开接口和需要认证的接口
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiFullIntegrationTest {

    @LocalServerPort
    private int port;

    // 存储登录后的 token
    private static String authToken;

    // 测试用户信息
    private static final String TEST_USERNAME = "test_" + (System.currentTimeMillis() % 100000);
    private static final String TEST_PASSWORD = "test123456";
    private static final String TEST_NICKNAME = "测试_" + (System.currentTimeMillis() % 100000);

    // 存储测试过程中创建的资源 ID
    private static Integer testUserId;
    private static Integer testVideoId = 1; // 假设数据库中存在视频
    private static Integer testFavoriteId;
    private static Integer testCommentId;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    // ==================== 第一步：用户注册和登录 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 用户注册")
    public void test_UserRegister() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", TEST_USERNAME);
        registerRequest.put("password", TEST_PASSWORD);
        registerRequest.put("nickname", TEST_NICKNAME);
        registerRequest.put("gender", 2);
        registerRequest.put("description", "这是一个测试账号");

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/api/user/register")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("1.2 检查用户名是否可用")
    public void test_CheckUsername() {
        given()
            .queryParam("username", TEST_USERNAME)
        .when()
            .get("/api/user/check-username")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", equalTo(false)); // 已注册，不可用
    }

    @Test
    @Order(3)
    @DisplayName("1.3 检查昵称是否可用")
    public void test_CheckNickname() {
        given()
            .queryParam("nickname", TEST_NICKNAME)
        .when()
            .get("/api/user/check-nickname")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", equalTo(false)); // 已注册，不可用
    }

    @Test
    @Order(4)
    @DisplayName("1.4 用户登录并提取 Token")
    public void test_UserLogin() {
        String response =
            given()
                .contentType(ContentType.URLENC)
                .formParam("username", TEST_USERNAME)
                .formParam("password", TEST_PASSWORD)
            .when()
                .post("/api/user/login")
            .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.token", notNullValue())
                .body("data.tokenType", equalTo("Bearer"))
                .body("data.user.username", equalTo(TEST_USERNAME))
                .extract().asString();

        // 提取 token 供后续测试使用
        authToken = io.restassured.path.json.JsonPath.from(response).getString("data.token");
        testUserId = io.restassured.path.json.JsonPath.from(response).getInt("data.user.uid");

        System.out.println("✅ 登录成功，Token: " + authToken);
        System.out.println("✅ 用户ID: " + testUserId);
    }

    // ==================== 第二步：公开接口测试 ====================

    @Test
    @Order(10)
    @DisplayName("2.1 获取视频列表（公开）")
    public void test_GetVideoList() {
        given()
            .queryParam("pageNum", 1)
            .queryParam("pageSize", 10)
        .when()
            .get("/api/video/list")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", notNullValue());
    }

    @Test
    @Order(11)
    @DisplayName("2.2 获取视频详情（公开）")
    public void test_GetVideoDetail() {
        given()
            .pathParam("vid", testVideoId)
        .when()
            .get("/api/video/detail/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(12)
    @DisplayName("2.3 按分类获取视频（公开）")
    public void test_GetVideosByCategory() {
        given()
            .queryParam("scId", "game")
            .queryParam("pageNum", 1)
            .queryParam("pageSize", 10)
        .when()
            .get("/api/video/category")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(13)
    @DisplayName("2.4 获取所有主分类（公开）")
    public void test_GetMainCategories() {
        given()
        .when()
            .get("/api/category/main")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", notNullValue());
    }

    @Test
    @Order(14)
    @DisplayName("2.5 获取子分类（公开）")
    public void test_GetSubCategories() {
        given()
            .pathParam("mcId", "游戏")
        .when()
            .get("/api/category/sub/{mcId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(15)
    @DisplayName("2.6 获取分类信息（公开）")
    public void test_GetCategoryInfo() {
        given()
            .pathParam("scId", "game")
        .when()
            .get("/api/category/info/{scId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(16)
    @DisplayName("2.7 获取推荐标签（公开）")
    public void test_GetRecommendTags() {
        given()
            .pathParam("scId", "game")
        .when()
            .get("/api/category/tags/{scId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(17)
    @DisplayName("2.8 综合搜索（公开）")
    public void test_Search() {
        given()
            .queryParam("keyword", "测试")
        .when()
            .get("/api/search")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data", notNullValue());
    }

    @Test
    @Order(18)
    @DisplayName("2.9 获取根评论列表（公开）")
    public void test_GetRootComments() {
        given()
            .queryParam("vid", testVideoId)
            .queryParam("pageNum", 1)
            .queryParam("pageSize", 10)
        .when()
            .get("/api/comment/root")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    // ==================== 第三步：需要认证的接口测试 ====================

    @Test
    @Order(20)
    @DisplayName("3.1 获取用户个人信息（需认证）")
    public void test_GetUserProfile() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("uid", testUserId)
        .when()
            .get("/api/user/profile/{uid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.uid", equalTo(testUserId))
            .body("data.username", equalTo(TEST_USERNAME));
    }

    @Test
    @Order(21)
    @DisplayName("3.2 更新用户个人信息（需认证）")
    public void test_UpdateUserProfile() {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("uid", testUserId);
        updateRequest.put("nickname", TEST_NICKNAME + "_updated");
        updateRequest.put("description", "更新后的个性签名");
        updateRequest.put("gender", 1);

        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/api/user/profile")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(22)
    @DisplayName("3.3 上传头像（需认证）")
    public void test_UploadAvatar() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.URLENC)
            .formParam("avatarUrl", "https://example.com/avatar.jpg")
        .when()
            .post("/api/user/avatar")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(23)
    @DisplayName("3.4 修改密码（需认证）")
    public void test_UpdatePassword() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.URLENC)
            .formParam("oldPassword", TEST_PASSWORD)
            .formParam("newPassword", TEST_PASSWORD + "_new")
        .when()
            .put("/api/user/password")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));

        // 改回原密码以便后续测试
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.URLENC)
            .formParam("oldPassword", TEST_PASSWORD + "_new")
            .formParam("newPassword", TEST_PASSWORD)
        .when()
            .put("/api/user/password")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(30)
    @DisplayName("3.5 创建收藏夹（需认证）")
    public void test_CreateFavorite() {
        Map<String, Object> favoriteRequest = new HashMap<>();
        favoriteRequest.put("name", "我的收藏夹_" + System.currentTimeMillis());
        favoriteRequest.put("description", "测试收藏夹");
        favoriteRequest.put("privacy", 0); // 公开

        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(favoriteRequest)
        .when()
            .post("/api/favorite/create")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(31)
    @DisplayName("3.6 获取收藏夹列表（需认证）")
    public void test_GetFavorites() {
        String response =
            given()
                .header("Authorization", "Bearer " + authToken)
            .when()
                .get("/api/favorite/list")
            .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue())
                .extract().asString();

        // 提取第一个收藏夹 ID 供后续测试使用
        try {
            testFavoriteId = io.restassured.path.json.JsonPath.from(response).getInt("data[0].fid");
            System.out.println("✅ 收藏夹ID: " + testFavoriteId);
        } catch (Exception e) {
            System.out.println("⚠️ 未找到收藏夹");
        }
    }

    @Test
    @Order(40)
    @DisplayName("3.7 点赞视频（需认证）")
    public void test_LikeVideo() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
        .when()
            .post("/api/video/interaction/like/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(41)
    @DisplayName("3.8 取消点赞视频（需认证）")
    public void test_UnlikeVideo() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
        .when()
            .post("/api/video/interaction/unlike/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(42)
    @DisplayName("3.9 投币（需认证）")
    public void test_CoinVideo() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
            .queryParam("count", 1)
        .when()
            .post("/api/video/interaction/coin/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(43)
    @DisplayName("3.10 收藏视频（需认证）")
    public void test_CollectVideo() {
        Assumptions.assumeTrue(testFavoriteId != null, "需要先创建收藏夹");

        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
            .queryParam("fid", testFavoriteId)
        .when()
            .post("/api/video/interaction/collect/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(44)
    @DisplayName("3.11 取消收藏视频（需认证）")
    public void test_UncollectVideo() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
        .when()
            .post("/api/video/interaction/uncollect/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(45)
    @DisplayName("3.12 记录播放（需认证）")
    public void test_RecordPlay() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
        .when()
            .post("/api/video/interaction/play/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(46)
    @DisplayName("3.13 获取用户视频行为（需认证）")
    public void test_GetUserVideoBehavior() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("vid", testVideoId)
        .when()
            .get("/api/user/video/behavior/{vid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(50)
    @DisplayName("3.14 发布评论（需认证）")
    public void test_PostComment() {
        Map<String, Object> commentRequest = new HashMap<>();
        commentRequest.put("vid", testVideoId);
        commentRequest.put("content", "这是一个测试评论_" + System.currentTimeMillis());
        commentRequest.put("parentId", 0);   // ✅ 修复：根评论的 parentId 为 0
        commentRequest.put("toUserId", 0);   // ✅ 添加：根评论的 toUserId 为 0

        String response =
            given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(commentRequest)
            .when()
                .post("/api/comment/post")
            .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .extract().asString();

        System.out.println("✅ 评论发布成功");
    }

    @Test
    @Order(51)
    @DisplayName("3.15 点赞评论（需认证）")
    public void test_LikeComment() {
        // 假设评论 ID 为 1
        int commentId = 1;

        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("commentId", commentId)
        .when()
            .post("/api/comment/like/{commentId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(52)
    @DisplayName("3.16 取消点赞评论（需认证）")
    public void test_UnlikeComment() {
        // 假设评论 ID 为 1
        int commentId = 1;

        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("commentId", commentId)
        .when()
            .post("/api/comment/unlike/{commentId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    @Test
    @Order(60)
    @DisplayName("3.17 获取评论树（公开）")
    public void test_GetCommentTree() {
        // 假设评论 ID 为 1
        int rootId = 1;

        given()
            .pathParam("rootId", rootId)
        .when()
            .get("/api/comment/tree/{rootId}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    // ==================== 清理测试数据 ====================

    @Test
    @Order(90)
    @DisplayName("4.1 删除收藏夹（需认证）")
    public void test_DeleteFavorite() {
        Assumptions.assumeTrue(testFavoriteId != null, "需要先创建收藏夹");

        given()
            .header("Authorization", "Bearer " + authToken)
            .pathParam("fid", testFavoriteId)
        .when()
            .delete("/api/favorite/delete/{fid}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200));
    }

    // ==================== 测试说明 ====================

    /**
     * 测试执行流程：
     *
     * 1. 用户注册和登录（Order 1-4）
     *    - 注册新用户
     *    - 验证用户名和昵称
     *    - 登录并提取 JWT Token
     *
     * 2. 公开接口测试（Order 10-18）
     *    - 测试所有不需要认证的接口
     *    - 包括视频列表、分类、搜索等
     *
     * 3. 需要认证的接口测试（Order 20-60）
     *    - 使用提取的 Token 测试需要认证的接口
     *    - 包括用户信息、视频互动、收藏夹、评论等
     *
     * 4. 清理测试数据（Order 90+）
     *    - 删除测试过程中创建的资源
     *
     * 注意事项：
     * - 所有测试使用真实的后端逻辑，不 mock 任何 Service
     * - 测试按照 @Order 注解的顺序执行
     * - Token 在第一次登录时提取，后续测试复用
     * - 部分测试依赖数据库中已存在的数据（如视频、评论等）
     * - 可根据实际数据库情况调整 testVideoId 等变量
     */
}
