import useSWR from "swr";

interface UserInfo {
  email: string;
  name: string;
}

const fetcher = async (url: string) => {
  const response = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
  });

  if (response.status === 401) {
    throw new Error("Unauthorized");
  }

  if (!response.ok) {
    throw new Error("Failed to fetch user details");
  }

  return response.json();
};

export function useAuth() {
  const {
    data: user,
    error,
    mutate,
  } = useSWR<UserInfo>(
    `${process.env.NEXT_PUBLIC_BACK_END_BASE_URL}/api/auth/user`,
    fetcher,
    {
      revalidateOnFocus: false,
      revalidateOnReconnect: true,
    }
  );
  const loading = !user && !error;
  const authenticated = !!user;

  async function logout(redirect: string = "/") {
    console.log("Logging out user", user?.name);
    // Implement serverLogout logic
    if (!error) {
      await fetch("/api/logout", {
        method: "POST",
      })
        .then(async (response) => {
          console.log("Response from server", response);
          if (!response.ok) {
            throw new Error("Failed to logout");
          }

          const result = await response.json();

          if (result.redirectUrl) {
            console.log("Redirecting to [client side]:", result.redirectUrl);
            window.location.href = result.redirectUrl;
          } else {
            window.location.pathname = redirect;
            mutate();
          }
        })
        .catch((error) => console.error("Failed to logout", error));
    }
  }

  return {
    authenticated,
    user,
    loading,
    mutate,
    error,
    logout,
  };
}
