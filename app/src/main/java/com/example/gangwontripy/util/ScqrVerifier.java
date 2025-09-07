package com.example.gangwontripy.util;

import android.util.Base64;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class ScqrVerifier {

    // 서버 application.yml 의 scqr.publicKeyPem 과 동일(PEM → DER Base64)
    private static final String PUBLIC_KEY_PEM =
            "-----BEGIN PUBLIC KEY-----"+
    "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEDp1L2mFVONodzw+GpiWKA/m4kYcQ"+
    "tzr087ZzEShA2RY/NW0nF8zih2b79h00XmYpqfw/zdYqJe3Cq+9v13HGfA=="+
            "-----END PUBLIC KEY-----";



    private static PublicKey publicKey;

    private static PublicKey loadPublicKey() throws Exception {
        if (publicKey != null) return publicKey;
        String spki = PUBLIC_KEY_PEM.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s","");
        byte[] der = android.util.Base64.decode(spki, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("EC");
        publicKey = kf.generatePublic(new X509EncodedKeySpec(der));
        return publicKey;
    }

    private static byte[] b64uDecode(String s){
        // URL-safe Base64, no padding
        return java.util.Base64.getUrlDecoder().decode(s);
    }

    public static Result verify(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 2) throw new IllegalArgumentException("bad token");
        byte[] payload = b64uDecode(parts[0]);
        byte[] sig = b64uDecode(parts[1]);

        Signature sg = Signature.getInstance("SHA256withECDSA");
        sg.initVerify(loadPublicKey());
        sg.update(payload);
        if (!sg.verify(sig)) throw new IllegalArgumentException("invalid signature");

        String json = new String(payload, StandardCharsets.UTF_8);
        JSONObject obj = new JSONObject(json);

        long exp = obj.optLong("exp", 0);
        if (exp > 0 && (System.currentTimeMillis()/1000L) > exp)
            throw new IllegalStateException("expired");

        return new Result(obj, token);
    }

    public static class Result {
        public final JSONObject payload;
        public final String token;
        public Result(JSONObject p, String t){ this.payload = p; this.token = t; }
        public String title(){ return payload.optString("title", ""); }
        public double lat(){ return payload.has("lat")? payload.optDouble("lat") : Double.NaN; }
        public double lng(){ return payload.has("lng")? payload.optDouble("lng") : Double.NaN; }
        public int radiusM(){ return payload.optInt("radiusM", 0); }
    }
}
