package com.radicle.payments.common.conf.token;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "gaia")
public class GaiaSettings {
	private String challengeText;
	private String readUrlPrefix;
	private String latestAuthVersion;
	private String storageRootDirectory;
	private String[] whitelist;

	public String getChallengeText() {
		return challengeText;
	}

	public void setChallengeText(String challengeText) {
		this.challengeText = challengeText;
	}

	public String getReadUrlPrefix() {
		return readUrlPrefix;
	}

	public void setReadUrlPrefix(String readUrlPrefix) {
		this.readUrlPrefix = readUrlPrefix;
	}

	public String getLatestAuthVersion() {
		return latestAuthVersion;
	}

	public void setLatestAuthVersion(String latestAuthVersion) {
		this.latestAuthVersion = latestAuthVersion;
	}

	public String getStorageRootDirectory() {
		return storageRootDirectory;
	}

	public void setStorageRootDirectory(String storageRootDirectory) {
		this.storageRootDirectory = storageRootDirectory;
	}

	public String[] getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(String[] whitelist) {
		this.whitelist = whitelist;
	}

}
