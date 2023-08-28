package com.beagledata.gaea.gateway.filter.postrequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * 描述:
 *
 * @author 周庚新
 * @date 2020-11-06
 */
public class CustomResetInputStream extends ServletInputStream {
	private byte[] data;
	private int idx = 0;

	public CustomResetInputStream(byte[] data) {
		if (data == null) {
		    data = new byte[0];
		}
		this.data = data;
	}


	@Override
	public int read() throws IOException {
		if (idx == data.length) {
		    return -1;
		}
		return data[idx++] & 0xff;
	}

	@Override
	public synchronized void reset() throws IOException {
		idx = 0;
	}
	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setReadListener(ReadListener listener) {

	}

}