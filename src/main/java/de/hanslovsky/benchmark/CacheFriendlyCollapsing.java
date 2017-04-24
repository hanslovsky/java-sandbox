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
import org.openjdk.jmh.runner.options.TimeValue;

import net.imglib2.Cursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

@BenchmarkMode( Mode.SingleShotTime )
@OutputTimeUnit( TimeUnit.MILLISECONDS )
@State( Scope.Benchmark )
public class CacheFriendlyCollapsing
{

	long[] size = new long[] { 128, 128, 64 };
//	long[] size = new long[] { 1 };

	long numChannels = 3;

	long[] sizeChannelsFirst = LongStream.concat( LongStream.of( numChannels ), Arrays.stream( size ) ).toArray();

	long[] sizeChannelsLast = LongStream.concat( Arrays.stream( size ), LongStream.of( numChannels ) ).toArray();

	ArrayImg< DoubleType, DoubleArray > channelFirst = ArrayImgs.doubles( sizeChannelsFirst );

	ArrayImg< DoubleType, DoubleArray > channelLast = ArrayImgs.doubles( sizeChannelsLast );

	ArrayImg< DoubleType, DoubleArray > targetChannelFirst = ArrayImgs.doubles( size );

	ArrayImg< DoubleType, DoubleArray > targetChannelLast = ArrayImgs.doubles( size );

	@Benchmark
	public void collapseFirst( final Blackhole blackhole )
	{
		final CompositeIntervalView< DoubleType, RealComposite< DoubleType > > source = Views.collapseReal( channelFirst, 0 );
		final Cursor< RealComposite< DoubleType > > s = Views.flatIterable( source ).cursor();
//		final ArrayCursor< DoubleType > t = targetChannelFirst.cursor();
		double sum = 0.0;
		while ( s.hasNext() )
		{
			sum = 0.0;
			final RealComposite< DoubleType > col = s.next();
			for ( int i = 0; i < numChannels; ++i )
				sum += col.get( i ).get();
//			t.next().set( sum );
		}
		blackhole.consume( sum );
	}

	@Benchmark
	public void collapseLast( final Blackhole blackhole )
	{
		final CompositeIntervalView< DoubleType, RealComposite< DoubleType > > source = Views.collapseReal( channelLast );
		final Cursor< RealComposite< DoubleType > > s = Views.flatIterable( source ).cursor();
//		final ArrayCursor< DoubleType > t = targetChannelFirst.cursor();
		double sum = 0.0;
		while ( s.hasNext() )
		{
			sum = 0.0;
			final RealComposite< DoubleType > col = s.next();
			for ( int i = 0; i < numChannels; ++i )
				sum += col.get( i ).get();
//			t.next().set( sum );
		}
		blackhole.consume( sum );
	}

	public static void main( final String[] args ) throws RunnerException, IOException
	{
		final Options opts = new OptionsBuilder()
				.include( ".*collapse.*" )
				.warmupIterations( 10 )
				.measurementIterations( 10 )
				.warmupBatchSize( 100 )
				.measurementBatchSize( 100 )
				.jvmArgs( "-server" )
				.forks( 1 )
				.warmupTime( TimeValue.seconds( 10 ) )
				.measurementTime( TimeValue.seconds( 10 ) )
				.build();

		new Runner( opts ).run();
	}

}
