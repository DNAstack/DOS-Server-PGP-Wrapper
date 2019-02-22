package com.dnastack.pgp.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ga4ghDataObject {
	private String id;
	private String name;
	private String size;
	private String created;
	private String updated;
	private String version;
	private String mimeType;
	private List<Checksum> checksums;
	private List<DosUrl> urls;
	private String description;
	private List<String> aliases;
}
