package int_.esa.eo.ngeo.dmtu.download.schedule;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDownloadProcess implements IDownloadProcess {
	private int progress = 0;
	private EDownloadStatus status = EDownloadStatus.NOT_STARTED;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TestDownloadProcess.class);

	@Override
	public EDownloadStatus cancelDownload() throws DMPluginException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() throws DMPluginException {
		// TODO Auto-generated method stub

	}

	@Override
	public File[] getDownloadedFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EDownloadStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EDownloadStatus pauseDownload() throws DMPluginException {
		status = EDownloadStatus.PAUSED;
		LOGGER.debug(String.format("Download paused at %s%%", progress));

		return null;
	}

	@Override
	public EDownloadStatus resumeDownload() throws DMPluginException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EDownloadStatus startDownload() throws DMPluginException {
		if(progress > 0) {
			LOGGER.debug(String.format("Resuming thread at %s%%", progress));
		}
		status = EDownloadStatus.RUNNING;
		while (progress < 100 && status == EDownloadStatus.RUNNING) {
			calculatePi();
			progress++;
		}
		if (status == EDownloadStatus.RUNNING) {
			status = EDownloadStatus.COMPLETED;
			LOGGER.debug("Progress complete");
		}

		return null;
	}

	private void calculatePi() {
		int NoOfTerms = 2000;
		int max = 5;

		NumberFormat df = DecimalFormat.getInstance();
		df.setMaximumFractionDigits(max);
		df.setRoundingMode(RoundingMode.HALF_UP);

		BigDecimal oddTerm = new BigDecimal("1");
		BigDecimal pi = new BigDecimal("0.0");
		final BigDecimal NEG_FOUR = new BigDecimal("-4");
		final BigDecimal POS_FOUR = new BigDecimal("4");
		final BigDecimal INC_TWO = new BigDecimal("2");

		int scale = 1000;// can be adjusted, higher for more dp, lower for less

		for (int i = 1; i <= NoOfTerms; i++) {
			BigDecimal currentTerm = new BigDecimal("0.0");

			if (i % 2 == 0) { // if an even term in sequence, it's negative
				currentTerm = NEG_FOUR.divide(oddTerm, scale,
						RoundingMode.HALF_UP);
			} else { // else, must be odd term, so it's positive
				currentTerm = POS_FOUR.divide(oddTerm, scale,
						RoundingMode.HALF_UP);
			}
			oddTerm = oddTerm.add(INC_TWO);
			pi = pi.add(currentTerm);
		}
//		System.out.println("Pi summation to Scale of " + scale + " : " + pi);
//		System.out.println("Pi rounded to " + max + " decimal places: "
//				+ df.format(pi));
	}

}
