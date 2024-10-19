import { cookies } from 'next/headers';
import { NextRequest, NextResponse } from 'next/server';

export async function PUT(request: NextRequest, { params }: { params: { slug: string } }) {
  const chatId = params.slug;
  const messages = await request.json();
  const cookieStore = cookies();
  const JSSESSION = cookieStore.get('JSESSIONID')
  const csrfToken = cookieStore.get('XSRF-TOKEN')

  if (!csrfToken) {
    console.log('CSRF token not found');
    return NextResponse.json({ error: 'CSRF token not found' }, { status: 400 });
}

  // Make the PUT request to the backend API
  try {
    const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/conversations/${chatId}/messages`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': csrfToken.value,
        'Cookie': `XSRF-TOKEN=${csrfToken?.value}; JSESSIONID=${JSSESSION?.value}`
      },
      credentials: 'include',
    body: JSON.stringify(messages)
    });

    if (!response.ok) {
      throw new Error('Failed to update messages');
    }

    return NextResponse.json({ status: 200 });
  } catch (error) {
    console.error('Error updating messages:', error);
    return NextResponse.json({ error: 'Failed to update messages' }, { status: 500 });
  }
}

export async function GET(request: NextRequest, { params }: { params: { slug: string } }) {
  const chatId = params.slug;
  const cookieStore = cookies();
  const JSSESSION = cookieStore.get('JSESSIONID')
  // Make the GET request to the backend API
  try {
    const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/conversations/${chatId}/messages`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': `JSESSIONID=${JSSESSION?.value}`
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Failed to fetch messages');
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error fetching messages:', error);
    return NextResponse.json({ error: 'Failed to fetch messages' }, { status: 500 });
  }
}