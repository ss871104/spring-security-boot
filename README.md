# Spring Boot, Spring Secuity, JWT Token, Spring Cache with Redis, Spring Validation

## **Index**
* [Quick Start](#quick-start)
* [Spring Security](#spring-security)
    * [JWT Token](#jwt-token)
    * [OAuth2](#oauth2)
* [Spring Cache with Redis](#spring-cache-with-redis)
* [Spring Validation](#spring-validation)

## **Quick Start**
1. 先將 main branch clone 到 local
2. import existing maven project 到你的 IDE
3. install [Homebrew](https://brew.sh/index)
4. install redis from homebrew
```console
brew install redis
```
5. start redis
```console
brew services start redis
```
6. run project server

執行完畢後可開啟 Postman 測試 api，以下 api 可供測試：
* 註冊帳號<br>
POST Method: http://localhost:8080/api/auth/register
```json
{
    "name": "andy",
    "username": "andyuser",
    "password": "password",
    "email": "andy@gmail.com"
}
```
* 登入<br>
POST Method: http://localhost:8080/api/auth/login
```json
{
    "username": "你的帳號或信箱",
    "password": "你的密碼"
}
```
* 查 user by id，可留意第一次查詢和第二次查詢的 console log<br>
GET Method: http://localhost:8080/api/user/1<br>
(需將註冊或登入獲得的 token 放到 header 裡)
```
'Authorization': 'Bearer {your_token}'
```

---

## **Spring Security**
Spring Security 是一個安全框架，它的函示庫提供了驗證（authentication）與授權（authorization）等有關安全管理的功能。
<br>Spring Security 框架解析可參考 [Wayne's Talk - Spring Security 架構解析](https://waynestalk.com/spring-security-architecture-explained/)

5.7.0-M2 版本開始(Spring Boot 2.7.0 之前的版本)，Spring Security 不建議使用 WebSecurityConfigureAdapter，將以以下配置為主：
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final DaoAuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomAuthEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // turn off csrf, or will be 403 forbidden
                .csrf().disable()
                // to allow <frame> elements (h2 console UI) from the same origin
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeRequests()
                .antMatchers("/api/auth/*").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/user/testAccessDenied").hasAnyRole("ADMIN")
                .anyRequest().authenticated().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .accessDeniedHandler(accessDeniedHandler).and()
                .httpBasic();

        return http.build();
    }
}
```

### **JWT Token**
Json Web Token(JWT) 是將 Header, Payload, Signature 分別以 base64 編譯後並以 . 組合成的一組字串，其特性為 Stateless 無狀態，意味著每一次從 Client 端向 Server 端發出的 request 都是獨立的。

#### **JWT v.s. Session**
Session 基於 Cookie，指的是在網路上的「狀態」，由於每次登入，Server 會產生一筆 Session 記錄在 Server 端，同時把身份特徵 SessionID 送回去 Client 端加密保存。想要清除的話，除了等待 Session 過期，還可以使用登出 (由 Server 來幫你消除 Session)，另外也可以從 Client 端來清除 Cookie (SessionID)。

Session 優點：
1. 統一由 Server 管理資料，安全性較有保證，是否清除資料也由 Server 控管，管理方便

Session 缺點：
1. 由於每次登入 Server 都要建立一筆 Session 存放，因此對 Server 的負擔較大
2. 跨域請求有 Session 丟失的問題

JWT 優點：
1. 由於 JWT 在 Server 產出後不需被存放在 Server，只需傳到 Client 端，因此對 Server 無負擔
2. 跨域請求中不受阻礙
3. 去中心化，便於分佈式架構

JWT 缺點：
1. 由於 JWT 不受 Server 控管，因此在 JWT 時效範圍內，任何持有有效 JWT 的人都可以向 Server 發出請求，安全性較低
2. 手動登出後，其 JWT 仍存在

#### **JWT 問題之解決方案**
可在 JWT 生成後同步將 JWT 存放至 redis 資料庫，並且存放在 redis 的有效期限大於 JWT 本身有效期限。每當 Client 端攜帶 JWT 丟 request 時，會先行確認 token 是否合法，並且確認是否過期，若是過期了便會到 redis 確認是否有紀錄此 token，若無，將重新登入，若有，生成新的 JWT 給 Client 端並且紀錄新的 JWT 至 redis 資料庫並且刪除原本的 JWT。登出時可從 redis 刪除 JWT key 值方式來確保登出。

此方案能一定程度解決 JWT 的缺點，仍然會有其他問題存在。


### **OAuth2**
OAuth 2.0 是一個授權協議，允許讓第三方應用以有限的權限訪問 HTTP 服務，可以透過構建資源擁有者與 HTTP 服務間的許可交互機制，讓第三方應用代表資源擁有者訪問服務，或者通過授予權限給第三方應用，讓其代表自己訪問服務 (透過 token)。

簡單來說就是第三方登入，例如在其他網站以 Google 登入


---

## **Spring Cache with Redis**
Spring Cache 是利用 Cache 快取機制將第一次查詢所得到的資料存放在 redis，在下一次做一樣的查詢時可直接從 redis 透過 key-value 迅速回傳查詢結果。此方法可以有效減少Database的溝通次數，也可以大幅減少查詢資料時的等待時間，提供良好的使用者體驗。

### **常見查詢方法的 Cache 應用：**

* findAll

設定 @Cacheable，當執行此方法時，若名為 UserList 的快取資料存在時，則不會執行此方法，直接從Cache取出資料。
```java
@Cacheable(value = "UserList")
public List<UserResponse> findAll() {
    List<User> userList = new ArrayList<>();
    userRepository.findAll().forEach(userList::add);
    return maptoDtoList(userList);
}
```

* findById

與 findAll 的設定一樣，只是這邊是針對單筆資料進行快取，因此名稱設定為 User，id 為 key 值。
```java
@Cacheable(value = "User", key = "#id")
public UserResponse findById(Long id) {
    return maptoDto(userRepository.findById(id));
}
```

* save(update)

因為會同時影響快取資料 User 及 UserList，因此透過@CachePut進行單筆快取資料更新，透過 @CacheEvict 將 UserList 的快取資料清空，這樣子下次執行 findAll 時，即會拿到最新的資料。
```java
@Caching(
        put = {@CachePut(value = "User", key = "#userRequest.id")},
        evict = {@CacheEvict(value = "UserList", allEntries = true)}
)
public void updateById(UserRequest userRequest) {
    User user = maptoUser(userRequest);
    userRepository.save(user);
}
```

* deleteById

刪除與save(update)類似，差別在於針對單筆快取及清單快取資料做刪除
```java
@Caching(
        evict = {@CacheEvict(value = "User", key = "#id"),
                @CacheEvict(value = "UserList", allEntries = true)}
)
public void deleteById(Long id) {
    userRepository.deleteById(id);
}
```


---

## **Spring Validation**
Spring Validation 是以 Server 端進行驗證的工具。

引入以下 dependency
```xml
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### **基本 annotation:**

|annotation|description|
|---|---|
|@NotNull / @Null|不得為 Null/必須為 Null|
|@AssertFalse / @AssertTrue|必須為 False/True|
|@Min / @Max|必須為數字類型，並且限制最大最小值|
|@DecimalMin / @DecimalMax|內容必須為數字，可接受字串，並且限制最大最小值|
|@Size(max,min)|限制內容長度，接受字串、Map、List 等等有 size 概念的類別|
|@NotBlank|必須為字串，必須含有至少一個非空白字元，且不得為 Null|
|@NotEmpty|接受字串、Map、List 等等有 empty 概念的類別，不得為空或 Null|
|@Digits(integer,fraction)|內容必須為數字，指定位數的最大長度，integer 表整數部分、fraction 表小數部分|
|@Email|字串必須為 email 格式|
|@Negative / @Positive|必須為數字類型，指定正或負值|
|@NegativeOrZero / @PositiveOrZero|必須為數字類型，指定正或負值，且接受零|
|@Pattern(regexp)|字串須符合 regular expression|
|@Future / @Past|日期或時間類型，必須為 未來/過去 的時間|
|@FutureOrPresent / @PastOrPresent|日期或時間類型，必須為 未來/過去 或當下的時間|

另外有 hibernate.validator 的 annotation:

|annotation|description|
|---|---|
|@Range(min,max)|內容須為數字，可以視為 @Max 跟 @Min 的合用|
|@Length(min,max)|專用於 String 的 @Size|
|@URL(protocol,host,port)|必須為字串，判別 URL 格式，且可以指定 protocol、host、port 不指定則為任意|


### **分組驗證**

宣告幾個不同的 interface 來用作區別，然後在各個 annotation 中指定到 groups 的參數中
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @NotBlank(message = "姓名不得為空", groups = Register.class)
    private String name;
    @Size(min = 8,  max = 20, message = "帳號長度不得低於 8 或大於 20", groups = {Register.class. Login.class})
    private String username;
    @Size(min = 8,  max = 20, message = "密碼長度不得低於 8 或大於 20", groups = {Register.class. Login.class})
    private String password;
    @Size(max = 255, message = "Email 長度不得大於 255")
    @Email(message = "Email 格式不正確")
    @NotBlank(message = "信箱不得為空", groups = Register.class)
    private String email;

    public interface Register extend Default{}
    public interface Login extend Default{}
}
```
```java
@PostMapping("/register")
@ResponseStatus(HttpStatus.NO_CONTENT)
public ResponseEntity<AuthenticationResponse> register(@RequestBody @Validated(UserDto.Register.class) RegisterRequest request) {
    return ResponseEntity.ok(authenticationService.register(request));
}
@PostMapping("/login")
@ResponseStatus(HttpStatus.OK)
public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Validated(UserDto.Login.class) LoginRequest request) {
    return ResponseEntity.ok(authenticationService.login(request));
}
```


### **巢狀驗證**
```java
@Data
public class UserDto {
    @Valid
    private UserProperty property;

    public interface Create{}
    public interface Update{}
}

@Data
public class UserProperty{
    @NotBlank(groups = UserDto.Create.class)
    @Size(max = 255)
    private String name;

    @NotBlank(groups = UserDto.Create.class)
    @Null(groups = Update.class)
    private String password;

    @NotBlank(groups = UserDto.Create.class)
    @Size(max = 255, groups={UserDto.Create.class, UserDto.Update.class})
    @Email()
    private String email;
}
```
```java
@PostMapping("")
@ResponseStatus(HttpStatus.CREATED)
public void createUser(@RequestBody @Validated(UserDto.Create.class) UserDto userDco){
}
```


### **客製化驗證**
參考 [Bingdoal - Spring boot Validation 進階操作: 客製化驗證、手動驗證](https://bingdoal.github.io/backend/2021/10/spring-boot-validation-customize-validator-and-annotation/)