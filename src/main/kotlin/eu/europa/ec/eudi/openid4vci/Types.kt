/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.openid4vci

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.oauth2.sdk.`as`.ReadOnlyAuthorizationServerMetadata
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URL
import java.security.cert.X509Certificate

const val FORMAT_MSO_MDOC = "mso_mdoc"
const val FORMAT_SD_JWT_VC = "vc+sd-jwt"
const val FORMAT_W3C_JSONLD_DATA_INTEGRITY = "ldp_vc"
const val FORMAT_W3C_JSONLD_SIGNED_JWT = "jwt_vc_json-ld"
const val FORMAT_W3C_SIGNED_JWT = "jwt_vc_json"

/**
 * A [URI] that strictly uses the 'https' protocol.
 */
@JvmInline
value class HttpsUrl private constructor(val value: URL) {

    override fun toString(): String = value.toString()

    companion object {

        /**
         * Parses the provided [value] as a [URI] and tries creates a new [HttpsUrl].
         */
        operator fun invoke(value: String): Result<HttpsUrl> = runCatching {
            val uri = URI.create(value)
            require(uri.scheme.contentEquals("https", true)) { "URL must use https protocol" }
            HttpsUrl(uri.toURL())
        }
    }
}

@JvmInline
@Serializable
value class CredentialConfigurationIdentifier(val value: String) {
    init {
        require(value.isNotEmpty()) { "value cannot be empty" }
    }
}

@JvmInline
@Serializable
value class CredentialIdentifier(val value: String) {
    init {
        require(value.isNotEmpty()) { "value cannot be empty" }
    }
}

/**
 * Domain object to describe a valid PKCE verifier
 */
data class PKCEVerifier(
    val codeVerifier: String,
    val codeVerifierMethod: String,
) {
    init {
        require(codeVerifier.isNotEmpty()) { "Code verifier must not be empty" }
        require(codeVerifierMethod.isNotEmpty()) { "Code verifier method must not be empty" }
    }
}

/**
 * Domain object to describe a valid issuance access token
 */
@JvmInline
value class AccessToken(val accessToken: String) {
    init {
        require(accessToken.isNotEmpty()) { "Access Token must not be empty" }
    }
}

/**
 * Authorization code to be exchanged with an access token
 */
@JvmInline
value class AuthorizationCode(val code: String) {
    init {
        require(code.isNotEmpty()) { "Authorization code must not be empty" }
    }
}

/**
 * A c_nonce related information as provided from issuance server.
 *
 * @param value The c_nonce value
 * @param expiresInSeconds  Nonce time to live in seconds.
 */
data class CNonce(
    val value: String,
    val expiresInSeconds: Long? = 5,
) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
    }
}

/**
 * An identifier of a Deferred Issuance transaction.
 *
 * @param value The identifier's value
 */
@JvmInline
value class TransactionId(val value: String) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
    }
}

/**
 * An identifier to be used in notification endpoint of issuer.
 *
 * @param value The identifier's value
 */
@JvmInline
value class NotificationId(val value: String) {
    init {
        require(value.isNotEmpty()) { "Value cannot be empty" }
    }
}

/**
 * A sealed hierarchy that defines the different key formats to be used in order to construct a Proof of Possession.
 */
sealed interface BindingKey {

    /**
     * A JWK biding key
     */
    data class Jwk(
        val jwk: JWK,
    ) : BindingKey {
        init {
            require(!jwk.isPrivate) { "Binding key of type Jwk must contain a public key" }
        }
    }

    /**
     * A Did biding key
     */
    data class Did(
        val identity: String,
    ) : BindingKey

    /**
     * An X509 biding key
     */
    data class X509(
        val chain: List<X509Certificate>,
    ) : BindingKey {
        init {
            require(chain.isNotEmpty()) { "Certificate chain cannot be empty" }
        }
    }
}

data class IssuanceResponseEncryptionSpec(
    val jwk: JWK,
    val algorithm: JWEAlgorithm,
    val encryptionMethod: EncryptionMethod,
) {
    init {
        // Validate algorithm provided is for asymmetric encryption
        require(JWEAlgorithm.Family.ASYMMETRIC.contains(algorithm)) {
            "Provided encryption algorithm is not an asymmetric encryption algorithm"
        }
        // Validate algorithm matches key
        require(jwk.keyType == KeyType.forAlgorithm(algorithm)) {
            "Encryption key and encryption algorithm do not match"
        }
        // Validate key is for encryption operation
        require(jwk.keyUse == KeyUse.ENCRYPTION) {
            "Provided key use is not encryption"
        }
    }
}

/**
 * A credential identified as a scope
 */
@JvmInline
value class Scope(val value: String) {
    init {
        require(value.isNotEmpty()) { "Scope value cannot be empty" }
    }
}

typealias CIAuthorizationServerMetadata = ReadOnlyAuthorizationServerMetadata
