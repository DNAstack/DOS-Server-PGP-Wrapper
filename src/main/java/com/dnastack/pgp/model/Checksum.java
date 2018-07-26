package com.dnastack.pgp.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Checksum {

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		md5("md5"), multipart_md5("multipart-md5"), S3("S3"), sha256("sha256"), sha512("sha512");

		private String val;

		private Type(String val) {
			this.val = val;
		}
	}

	private String checksum;
	private Type type;
	
}
