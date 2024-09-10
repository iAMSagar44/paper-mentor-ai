import { NextResponse } from 'next/server';
import readNDJSONStream from "ndjson-readablestream";

export const dynamic = 'force-dynamic';
export const runtime = 'edge';

type RequestData = {
    question: string
}


export async function POST(request: Request) {

    const { readable, writable } = new TransformStream();
    const writer = writable.getWriter();

    const { question } = await request.json() as RequestData;

    const responseStream = await fetch(`${process.env.BACK_END_BASE_URL}/api/chat/stream`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ "question": question }),
    });

    if (!responseStream.body) {
        return new NextResponse('Failed to connect to SSE endpoint', { status: 500 });
    }

    async function push() {
        try {
            if (responseStream.body) {
                for await (const chunk of readNDJSONStream(responseStream.body)) {
                    //console.log("The streamed response is :", chunk);
                    await writer.write(`data: ${JSON.stringify(chunk)}\n\n`);
                }
            } else {
                throw new Error('Failed to connect to SSE endpoint');
            }
        } catch (error) {
            console.error('SSE error:', error);
        } finally {
            writer.close();
        }
    }

    push();

    request.signal.addEventListener('abort', () => {
        writer.close();
    });

    return new NextResponse(readable, {
        headers: {
            'Content-Type': 'text/event-stream; charset=utf-8',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive',
            'X-Accel-Buffering': 'no',
            'Transfer-Encoding': 'chunked',
            'X-Content-Type-Options': 'nosniff',
        }
    });
}