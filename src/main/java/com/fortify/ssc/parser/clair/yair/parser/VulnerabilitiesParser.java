package com.fortify.ssc.parser.clair.yair.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.FortifyAnalyser;
import com.fortify.plugin.api.FortifyKingdom;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.ssc.parser.clair.yair.CustomVulnAttribute;
import com.fortify.ssc.parser.clair.yair.domain.Vulnerability;
import com.fortify.util.ssc.parser.EngineTypeHelper;
import com.fortify.util.ssc.parser.ScanDataStreamingJsonParser;
import com.fortify.util.ssc.parser.VulnerabilityBuilder;
import com.fortify.util.ssc.parser.VulnerabilityBuilder.CustomStaticVulnerabilityBuilder;

public class VulnerabilitiesParser {
	private static final String ENGINE_TYPE = EngineTypeHelper.getEngineType();
	@SuppressWarnings("serial")
	private static final Map<String,Priority> CVE_SEVERITY_TO_PRIORITY_MAP = new HashMap<String, Priority>() {{
		put("Unknown", Priority.Medium);
		put("Negligible", Priority.Low);
		put("Low", Priority.Low);
		put("Medium", Priority.Medium);
		put("High", Priority.High);
		put("Critical", Priority.Critical);
		put("Defcon1", Priority.Critical);
	}};
	
	private final ScanData scanData;
	private final VulnerabilityBuilder vulnerabilityBuilder;

    public VulnerabilitiesParser(final ScanData scanData, final VulnerabilityBuilder vulnerabilityBuilder) {
    	this.scanData = scanData;
		this.vulnerabilityBuilder = vulnerabilityBuilder;
	}
    
    /**
	 * Main method to commence parsing the SARIF document provided by the
	 * configured {@link ScanData}.
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public final void parse() throws ScanParsingException, IOException {
		new ScanDataStreamingJsonParser()
			.expectedStartTokens(JsonToken.START_ARRAY)
			.handler("/*", Vulnerability.class, this::buildVulnerability)
			.parse(scanData);
	}
	
	private final void buildVulnerability(Vulnerability vuln) {
    	String cve = vuln.getCve_name();
		String uniqueId = DigestUtils.sha256Hex(cve);
		CustomStaticVulnerabilityBuilder vb = vulnerabilityBuilder.startStaticVulnerability();
		// TODO For now we let CustomStaticVulnerabilityBuilder handle duplicate id's
		//      We should check whether there's a better way to generate unique id
		vb.setInstanceId(uniqueId);
		
		// Set meta-data
		vb.setEngineType(ENGINE_TYPE);
		vb.setKingdom(FortifyKingdom.ENVIRONMENT.getKingdomName());
		vb.setAnalyzer(FortifyAnalyser.CONFIGURATION.getAnalyserName());
		vb.setCategory("Insecure Deployment");
		vb.setSubCategory("Vulnerable Container");
		
		// Set mandatory values to JavaDoc-recommended values
		vb.setAccuracy(5.0f);
		vb.setConfidence(2.5f);
		vb.setLikelihood(2.5f);
		
		// Set standard vulnerability fields based on input
		vb.setFileName(vuln.getPackage_name());
		vb.setPriority(CVE_SEVERITY_TO_PRIORITY_MAP.getOrDefault(vuln.getCve_severity(), Priority.Medium));
		vb.setVulnerabilityAbstract(vuln.getCve_desc());
		
		// Set custom attributes based on input
		vb.setStringCustomAttributeValue(CustomVulnAttribute.namespace_name, vuln.getNamespace_name());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.package_name, vuln.getPackage_name());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.installed_version, vuln.getInstalled_version());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.cve_name, vuln.getCve_name());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.cve_severity, vuln.getCve_severity());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.cve_link, vuln.getCve_link());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.cve_fixed_version, vuln.getCve_fixed_version());
		
		vb.completeVulnerability();
    }
}
