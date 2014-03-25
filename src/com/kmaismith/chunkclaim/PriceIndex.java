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
	
	public BigDecimal getPI() {
		return config.getBigDecimal("pi", BigDecimal.ONE.negate());
	}
	
	public void setPI(double input) {
		config.setProperty("pi", BigDecimal.valueOf(input));
		config.removeProperty("pi");
		config.save();
	}

	@Override
	public void reloadConfig() {
		config.load();
	}
}
