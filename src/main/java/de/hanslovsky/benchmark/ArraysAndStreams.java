package de.hanslovsky.benchmark;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode( Mode.AverageTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@State( Scope.Benchmark )
public class ArraysAndStreams {

	private final long[] array = new long[] { 1, 2, 3 };

	@Benchmark
	public void createNewArrayAndCopy( final Blackhole blackhole ) {
		final long[] target = new long[ array.length + 1 ];
		System.arraycopy(array, 0, target, 0, array.length );
		target[ array.length ] = array.length;
		blackhole.consume( target );
	}

	@Benchmark
	public void streamAndConcatenate( final Blackhole blackhole ) {
		final long[] target = LongStream.concat( Arrays.stream( array ), LongStream.of( array.length ) ).toArray();
		blackhole.consume( target );
	}

	public static void main( final String[] args ) throws RunnerException, IOException {
		final Options opts = new OptionsBuilder()
				.include( ".*" )
				.warmupIterations( 10 )
				.measurementIterations( 10 )
				.warmupBatchSize( 1000 )
				.measurementBatchSize( 1000 )
				.jvmArgs( "-server" )
				.forks( 1 )
				.build()
				;

		new Runner( opts ).run();
	}

}
