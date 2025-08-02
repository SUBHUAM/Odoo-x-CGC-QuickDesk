"use client";
import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import AuthService from "../services/AuthService";
import { useRouter } from "next/navigation";

export default function ProfilePage() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        // Check user from session only
        const sessionUser = AuthService.getSessionUser();
        if (sessionUser) {
          setUser(sessionUser);
        } else {
          router.push("/login");
        }
      } catch (err) {
        console.error("Failed to load user:", err);
        router.push("/login");
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, [router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Loading...</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-red-500">No user data found. Please log in.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar dashboard="1" />

      <div className="max-w-lg mx-auto bg-white shadow p-6 mt-6 rounded">
        <h2 className="text-2xl font-bold mb-4">Profile</h2>

        {/* Name */}
        <div className="mb-4">
          <label className="block text-gray-700 mb-1">Name</label>
          <input
            type="text"
            value={user.name || ""}
            disabled
            className="w-full border p-2 rounded bg-gray-100"
          />
        </div>

        {/* Email */}
        <div className="mb-4">
          <label className="block text-gray-700 mb-1">Email</label>
          <input
            type="text"
            value={user.email || ""}
            disabled
            className="w-full border p-2 rounded bg-gray-100"
          />
        </div>

        {/* Role */}
        <div className="mb-4">
          <label className="block text-gray-700 mb-1">Role</label>
          <input
            type="text"
            value={user.role || ""}
            disabled
            className="w-full border p-2 rounded bg-gray-100"
          />
        </div>

        {/* Save Changes Button (Disabled) */}
        <button
          disabled
          className="w-full py-2 text-white rounded bg-gray-400 cursor-not-allowed"
        >
          Save Changes
        </button>

        {/* Logout Button */}
        <button
          onClick={async () => {
            await AuthService.logout();
            router.push("/login");
          }}
          className="w-full mt-3 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Logout
        </button>
      </div>
    </div>
  );
}
