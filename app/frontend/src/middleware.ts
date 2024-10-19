import { cookies } from 'next/headers';
import { NextRequest, NextResponse } from 'next/server';

interface UserInfo {
  email: string;
  name: string;
}

export async function middleware(req: NextRequest) {
  const cookieStore = cookies();
  const JSSESSION = cookieStore.get('JSESSIONID')
  try {
    console.log('Fetching user details');
      const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/auth/user`, {
          cache: 'no-store',
          headers: {  
              'Content-Type': 'application/json',
              'Cookie': `JSESSIONID=${JSSESSION?.value}`
          },
          credentials: 'include',
      });
      // If the user is not authenticated (401), redirect to the login page
      if (response.status === 401) {
        console.log('User is not authenticated');
        return NextResponse.redirect(new URL('/login', req.url));
      }
      // If the response is OK and user is present, allow the user to access the /profile route
      if (response.ok) {
        const user: UserInfo | null = await response.json().catch(() => null);
        if (!user) {
          console.log('User not found or is not authenticated');
          return NextResponse.redirect(new URL('/login', req.url));
        }
        console.log('User in middleware:', user);
        return NextResponse.next();
      }

      // If any other error occurs, handle it (optional)
      return NextResponse.redirect(new URL('/error', req.url));
  } catch (error) {
      console.error('Error fetching user details:', error);
      // Todo: Create an error page and redirect the user to it
    return NextResponse.redirect(new URL('/error', req.url));
  }
}

// Apply middleware only to the /profile route
export const config = {
  matcher: ['/profile'],
};