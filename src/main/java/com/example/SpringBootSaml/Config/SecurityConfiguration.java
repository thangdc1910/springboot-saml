package com.example.SpringBootSaml.Config;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {


    private static final String LOGOUT_CALLBACK_URL = "/logout/saml2/slo";

    @Value("${metadata.file}")
    private String METADATA_LOCATION;

    @Value("${private.key}")
    String privateKey;
    @Value("${public.certificate}")
    String publicCertificate;

    @Value("${registration.id}")
    private String registrationId;

    private static final String OKTA = "okta";
    private static final String AZURE = "azure";

    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();
        authenticationProvider.setResponseAuthenticationConverter(groupsConverter());

        http.csrf().disable();

        http.httpBasic().disable();

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/saml2/**").permitAll()
                        //to fix error The InResponseTo attribute [] does not match the ID ...
                        //.requestMatchers("/favicon.ico").permitAll()
                        // .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/**", "/**/*").hasAnyAuthority("Action_Group", "ReadOnly_Group")
                        //.requestMatchers(HttpMethod.POST, "/**").hasAnyAuthority("Action_Group")
                        //.requestMatchers(HttpMethod.POST, "/**/*").hasAnyAuthority("Action_Group")
                        .anyRequest().authenticated())
                .saml2Login(saml2 -> saml2
                        .authenticationManager(new ProviderManager(authenticationProvider)))
                //todo
                .saml2Logout((saml2) -> saml2
                        .logoutRequest((request) -> request.logoutUrl(LOGOUT_CALLBACK_URL))
                        .logoutResponse((response) -> response.logoutUrl(LOGOUT_CALLBACK_URL))
                );

        //it works in okta, but not work in azure? do not know why???? - azure automatically call /login after /logout -
       // http.logout().logoutSuccessUrl("http://localhost:8080").invalidateHttpSession(true).deleteCookies("JSESSIONID");


        return http.build();
    }

    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> groupsConverter() {

        Converter<ResponseToken, Saml2Authentication> delegate =
                OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return (responseToken) -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            List<String> groups = principal.getAttribute("groups");
            Set<GrantedAuthority> authorities = new HashSet<>();
            if (groups != null) {
                groups.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            } else {
                authorities.addAll(authentication.getAuthorities());
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), authorities);
        };
    }

    private X509Certificate getCertificate(String certificatePath) throws CertificateException, IOException {
        Resource resource = new ClassPathResource(certificatePath);

        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(resource.getInputStream());
    }

    private RSAPrivateKey getKey(String keyPath) throws IOException {
        Resource resource = new ClassPathResource(keyPath);

        return RsaKeyConverters.pkcs8().convert(resource.getInputStream());
    }


    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrations() throws IOException, CertificateException {
        Saml2X509Credential credential = Saml2X509Credential.signing(getKey(privateKey), getCertificate(publicCertificate));

        RelyingPartyRegistration registration = RelyingPartyRegistrations.fromMetadataLocation(METADATA_LOCATION)
                .registrationId(registrationId)
                //in azure i config entityID= batoac.com  - in okta i do not config
                .entityId(registrationId.equals(AZURE) ? "batoac.com" : String.format("{baseUrl}/saml2/service-provider-metadata/%s", registrationId))
                .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo")
                .singleLogoutServiceResponseLocation("/abc.com")
                .signingX509Credentials((signing) -> signing.add(credential))
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
}