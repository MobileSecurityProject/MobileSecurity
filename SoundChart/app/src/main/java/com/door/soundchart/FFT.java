package com.door.soundchart;
/**
    @ref: https://stackoverflow.com/questions/9272232/fft-library-in-android-sdk
 */

public class FFT {
    int n, m; // n is chunk size, usually 1024

    // Lookup tables. Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    public FFT(int n) {
        this.n = n;
        this.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if (n != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[n / 2];
        sin = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }

    }

    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        // Bit-reverse
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }
}


/*

Using the class at: https://www.ee.columbia.edu/~ronw/code/MEAPsoft/doc/html/FFT_8java-source.html

Short explanation: call fft() providing x as you amplitude data, y as all-zeros array, after the function returns your first answer will be a[0]=x[0]^2+y[0]^2.

Complete explanation: FFT is complex transform, it takes N complex numbers and produces N complex numbers. So x[0] is the real part of the first number, y[0] is the complex part. This function computes in-place, so when the function returns x and y will have the real and complex parts of the transform.

One typical usage is to calculate the power spectrum of audio. Your audio samples only have real part, you your complex part is 0. To calculate the power spectrum you add the square of the real and complex parts P[0]=x[0]^2+y[0]^2.

Also it's important to notice that the Fourier transform, when applied over real numbers, result in symmetrical result (x[0]==x[x.lenth-1]). The data at x[x.length/2] have the data from frequency f=0Hz. x[0]==x[x.length-1] has the data for a frequency equals to have the sampling rate (eg if you sampling was 44000Hz than it means f[0] refeers to 22kHz).

Full procedure:

create array p[n] with 512 samples with zeros
Collect 1024 audio samples, write them on x
Set y[n]=0 for all n
calculate fft(x,y)
calculate p[n]+=x[n+512]^2+y[n+512]^2 for all n=0 to 512
to go 2 to take another batch (after 50 batches go to next step)
plot p
go to 1
Than adjust the fixed number for your taste.

The number 512 defines the sampling window, I won't explain it. Just avoid reducing it too much.

The number 1024 must be always the double of the last number.

The number 50 defines you update rate. If your sampling rate is 44000 samples per second you update rate will be: R=44000/1024/50 = 0.85 seconds.


*/