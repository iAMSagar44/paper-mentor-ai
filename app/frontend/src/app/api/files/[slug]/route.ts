import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest, 
    { params }: { params: { slug: string } }
) {
    const fileName = params.slug;
    //console.log('Received request to fetch the file:', fileName);

    try {
        // Call the Spring Boot API to get the file
        const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/files/${fileName}`);

        if (!response.ok) {
            throw new Error('Failed to fetch the file');
        }

        return new NextResponse(response.body, {
            headers: {
                'Content-Type': response.headers.get('Content-Type') || 'application/octet-stream',
                'Content-Disposition': `inline; filename=${fileName}`
            }
        });
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to fetch the file' });
    }
}