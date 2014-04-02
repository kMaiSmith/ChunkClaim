package com.kmaismith.chunkclaim;

import java.io.File;
import java.math.BigDecimal;

public class PriceIndex implements IConfig {
	private final Config config;
	public PriceIndex(File dir) {
		config = new Config(new File(dir, "priceindex.yml"));
		config.setTemplateName("/priceindex.yml");
		config.load();
	}
	
	public String getPI() {
		return config.getBigDecimal("pi", BigDecimal.ONE.negate()).toPlainString();
	}
	
	public void setPI(BigDecimal input) {
		config.setProperty("pi", input);
		config.save();
	}

	@Override
	public void reloadConfig() {
		config.load();
	}
}
