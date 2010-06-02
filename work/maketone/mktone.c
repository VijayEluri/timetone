/*--------------------------------------------------------------*\
		module	:	nhk.c
		purpose :	NHK‚Ì•ñ‰¹‚ğ¶¬‚·‚é
		author	:	_–ì@Œ’Œá Kengo Jinno <KHB04045@nifty.ne.jp>
		create	:	2005/12/27
		update	:	2006/09/26
\*--------------------------------------------------------------*/

/*
	11025Hz 8bit ƒ‚ƒmƒ‰ƒ‹
*/

#include	<stdio.h>
#include	<math.h>

/*	‰~ü—¦ƒÎ	*/
#define		PIE		3.1415926535897932

#pragma pack(1)

typedef	struct	tagWaveHeaderType	{
	char	cRiffHeader[4];
	long	nFileSize;
	char	cWaveHeader[4];
	char	cFmtChunk[4];
	long	nChunkSize;
	short	nFmtID;
	short	nCh;
	long	nRate;
	long	nSpeed;
	short	nBlockSize;
	short	nBitsParSample;
	char	cDataChunk[4];
	long	nDataBytes;
}	WaveHeaderType, * LpWaveHeaderType;

#pragma pack()

void	InitHeader( LpWaveHeaderType p )
{
	p->cRiffHeader[0] = 'R';
	p->cRiffHeader[1] = 'I';
	p->cRiffHeader[2] = 'F';
	p->cRiffHeader[3] = 'F';

	p->nFileSize = 0;	/*	Œã‚ÅŒvZ	*/

	p->cWaveHeader[0] = 'W';
	p->cWaveHeader[1] = 'A';
	p->cWaveHeader[2] = 'V';
	p->cWaveHeader[3] = 'E';

	p->cFmtChunk[0] = 'f';
	p->cFmtChunk[1] = 'm';
	p->cFmtChunk[2] = 't';
	p->cFmtChunk[3] = ' ';

	p->nChunkSize = 16;

	p->nFmtID = 1;

	p->nCh = 1;
	p->nRate = 11025;
	p->nSpeed = 11025;
	p->nBlockSize = 1;
	p->nBitsParSample = 8;

	p->cDataChunk[0] = 'd';
	p->cDataChunk[1] = 'a';
	p->cDataChunk[2] = 't';
	p->cDataChunk[3] = 'a';

#if 0 /* ORIG */
	p->nDataBytes = 11025L*8L;
#else
	p->nDataBytes = 11025L*2L;
#endif
	p->nFileSize = p->nDataBytes + 44L - 8L;
}

/*
	–³‰¹	1•b
	—\‚P	1•b
	—\‚Q	1•b
	—\‚R	1•b
	–{M†	2•b
	Œ¸Š	1•b
	–³‰¹	1•b
*/

int		main( int ac, char* av[] )
{
	WaveHeaderType	h;
	int		i, j;
	long	x;
	char	c;
	FILE*	fp;

	fp = fopen( "tone.wav", "wb" );

	InitHeader( &h );

	fwrite( &h, sizeof(h), 1, fp );

#if 0
	/*	–³‰¹	1•b	*/
	c = 0x80;
	for( i = 0; i < 11025; i++ ) {
		fwrite( &c, sizeof(c), 1, fp );
	}

	/*	—\	1•b	~3	*/
	for( j = 0; j < 3; j++ ) {
		for( i = 0; i < 11025; i++ ) {
			if( i < 11025/10 ) {
				/*	100ms	*/
				c = (unsigned char)( 128.0 * sin( 2.0 * PIE * (double)i / ( 11025.0 / 440.0 ) ) + 128.0 );
			} else {
				/*	c‚è900ms‚Í–³‰¹	*/
				c = 0x80;
			}
			fwrite( &c, sizeof(c), 1, fp );
		}
	}
#endif

#if 0 /* ORIG */
	/*	–{M†	2•b	*/
	/*	Œ¸Š	1•b	*/
	for( x = 0; x < 11025L*3L; x++ ) {
		double	d;
		if( x < 11025L*2L ) {
			/*	2•bŠÔ‚Í–{M†‚ğƒtƒ‹ƒŒƒxƒ‹	*/
			d = 128.0;
		} else {
			/*	c‚è1•bŠÔ‚ÍŒ¸Š	*/
			long	n = 11025L*3L - x;
			d = 128.0 * (double)n / 11025L;
		}
		c = (unsigned char)( d * sin( 2.0 * PIE * (double)x / ( 11025.0 / 880.0 ) ) + 128.0 );
		fwrite( &c, sizeof(c), 1, fp );
	}
#else
	for( x = 0; x < 11025L*2L; x++ ) {
		double	d;
		if( x < 11025L ) {
			/*	2•bŠÔ‚Í–{M†‚ğƒtƒ‹ƒŒƒxƒ‹	*/
			d = 128.0;
		} else {
			/*	c‚è1•bŠÔ‚ÍŒ¸Š	*/
			long	n = 11025L*2L - x;
			d = 128.0 * (double)n / 11025L;
		}
		c = (unsigned char)( d * sin( 2.0 * PIE * (double)x / ( 11025.0 / 880.0 ) ) + 128.0 );
		fwrite( &c, sizeof(c), 1, fp );
	}
#endif

	/*	–³‰¹	1•b	*/
	c = 0x80;
	for( i = 0; i < 11025; i++ ) {
		fwrite( &c, sizeof(c), 1, fp );
	}

	fclose( fp );

	return( 0 );
}
