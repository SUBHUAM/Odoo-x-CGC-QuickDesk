"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import AuthService from "../services/AuthService";

export default function Navbar({ profile, dashboard }) {
  const router = useRouter();
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (profile) {
      const fetchUser = async () => {
        try {
          const userData = await AuthService.getCurrentUser();
          setUser(userData);
        } catch (err) {
          console.error("Failed to fetch user:", err);
        }
      };
      fetchUser();
    }
  }, [profile]);

  // Get first letter of user name
  const getInitial = (name) => (name ? name.charAt(0).toUpperCase() : "❔");

  // Assign background color dynamically
  const getBgColor = (name) => {
    const colors = [
      "bg-blue-500",
      "bg-green-500",
      "bg-purple-500",
      "bg-pink-500",
      "bg-yellow-500",
      "bg-red-500",
    ];
    if (!name) return "bg-gray-500";
    const index = name.charCodeAt(0) % colors.length;
    return colors[index];
  };

  return (
    <nav className="bg-blue-600 text-white px-6 py-4 shadow-md flex justify-between items-center">
      {/* Logo */}
      <h1 className="text-2xl font-bold">QuickDesk</h1>

      {/* Conditional Avatar */}
      {profile && user && (
        <div className="flex items-center space-x-4">
          {/* Dashboard Button */}
          {dashboard && (
            <button
              onClick={() => router.push("/dashboard")}
              className="px-4 py-2 bg-white text-blue-600 font-semibold rounded hover:bg-gray-100 transition"
            >
              Dashboard
            </button>
          )}
          <button
            onClick={() => router.push("/profile")}
            className={`w-10 h-10 flex items-center justify-center rounded-full font-bold ${getBgColor(
              user.name
            )}`}
          >
            {getInitial(user.name)}
          </button>
        </div>
      )}
    </nav>
  );
}
