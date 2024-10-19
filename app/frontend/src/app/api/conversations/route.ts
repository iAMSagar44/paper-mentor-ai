import { Conversation } from "@/app/lib/definitions";
import { cookies, headers } from "next/headers";
import { redirect } from "next/navigation";
import { NextRequest, NextResponse } from "next/server";
import { cache } from "react";

export async function GET(req: NextRequest) {

    const cookieStore = cookies();
    const JSSESSION = cookieStore.get('JSESSIONID')
    try {
        const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/conversations`, {
            cache: 'no-store',
            headers: {  
                'Content-Type': 'application/json',
                'Cookie': `JSESSIONID=${JSSESSION?.value}`
            },
            credentials: 'include',
        });

        if (response.status === 401) {
            // User is not authenticated
            console.log('User is not authenticated');
            return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
          }
        if (!response.ok) {
            console.log('Response from the server');
            return NextResponse.json({ error: 'Failed to fetch conversations' }, { status: 500 });
        }
        if (response.ok) {
            const conversations: Conversation[] = await response.json();
            //console.log('Conversations: [route handler]', conversations);
            return NextResponse.json({conversations}, { status: 200 });
        }
    } catch (error) {
        console.error('Error fetching conversations:', error);
        throw error;
    }
}