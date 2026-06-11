# MallCloud 鍚庣浠ｇ爜璐ㄩ噺涓庣姸鎬佸璁℃姤鍛?
**瀹℃煡鏃ユ湡**锛?026-06-11
**瀹¤鑼冨洿**锛氬悗绔墍鏈夊井鏈嶅姟 `mall-*/src/main/java` 婧愮爜鍙?Maven 閰嶇疆銆?
## 1. 娴嬭瘯涓庡亣鏁版嵁娓呯悊

閫氳繃鍏ㄥ眬姝ｅ垯妫€绱?`TODO|FIXME|mock|fake|dummy|涓存椂|娴嬭瘯鏁版嵁`锛?- **缁撴灉**锛? 涓尮閰嶃€?- **缁撹**锛氬悗绔唬鐮佹棤閬楃暀鐨勫紑鍙戞€?TODO 鏍囪锛屼复鏃剁‖缂栫爜鐨勬暟鎹拰 Mock 鎺ュ彛杩斿洖鍧囧凡娓呯悊銆?
## 2. 璋冭瘯杈撳嚭娓呯悊

閫氳繃鍏ㄥ眬姝ｅ垯妫€绱?`System.out.println`锛?- **缁撴灉**锛? 涓尮閰嶃€?- **缁撹**锛氭湇鍔″唴閮ㄩ€昏緫閬垮厤浜嗙洿鎺ユ墦鍗版爣鍑嗚緭鍑猴紝鏃ュ織缁熶竴閲囩敤 Slf4j + Logback 妗嗘灦杈撳嚭銆?
## 3. Maven 缂栬瘧鐘舵€?
閫氳繃鎵ц `mvn package -DskipTests`锛?- **缁撴灉**锛歚BUILD SUCCESS`锛孴otal time: 8.740 s銆?- **鏋勫缓璇︽儏**锛氭兜鐩?mall-common, mall-gateway, mall-auth, mall-user, mall-product, mall-inventory, mall-cart, mall-order, mall-pay, mall-search, mall-seckill, mall-message, mall-admin-biz, mall-job 鍏?14 涓井鏈嶅姟妯″潡鍙婂叕鍏辨ā鍧楋紝鍏ㄩ儴缂栬瘧鎵撳寘鎴愬姛銆?- **缁撹**锛氬悗绔」鐩棤缂栬瘧閿欒鎴栦緷璧栧啿绐侊紝鍏ㄦā鍧楀叿澶囬殢鏃朵笂绾跨殑鎵撳寘鏉′欢銆?
---
**瀹¤缁撹**锛氬悗绔唬鐮佺粨鏋勫仴搴凤紝鏃犳祴璇曟々鎴栬皟璇曞悗閬楃棁锛屾弧瓒崇敓浜у彂甯冪骇鐨勮川閲忚姹傘€?
