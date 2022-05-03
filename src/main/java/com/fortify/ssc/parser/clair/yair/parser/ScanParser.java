package com.fortify.ssc.parser.clair.yair.parser;

import java.io.IOException;
import java.util.Date;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;

public class ScanParser {
	//private final ScanData scanData;
    private final ScanBuilder scanBuilder;
    
	public ScanParser(final ScanData scanData, final ScanBuilder scanBuilder) {
		//this.scanData = scanData; // Input doesn't contain any useful scan data, so we don't use this
		this.scanBuilder = scanBuilder;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		scanBuilder.setScanDate(new Date()); // Required but not available in input
		scanBuilder.completeScan();
	}
}
