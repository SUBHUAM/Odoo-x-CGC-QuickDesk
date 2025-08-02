"use client";
import { useState } from "react";
import Navbar from "../components/Navbar";

export default function ProfilePage() {
  // Initial profile values (simulate fetched data)
  const [form, setForm] = useState({
    name: "John Doe",
    role: "End User",
    category: "Support",
    language: "English",
  });

  const [message, setMessage] = useState("");

  // Handle input change
  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // Simulate upgrade request
  const handleUpgrade = () => {
    setMessage("Upgrade request sent to admin.");
    setTimeout(() => setMessage(""), 3000);
  };

  // Floating label classes
  const labelClasses = (value) =>
    `absolute left-2 transition-all bg-white px-1
    ${value
      ? "top-[-8px] text-xs text-blue-500"
      : "peer-focus:top-[-8px] peer-focus:text-xs peer-focus:text-blue-500 top-2.5 text-gray-400 text-base"
    }`;

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navbar */}
      <Navbar />

      {/* Profile Form */}
      <div className="flex justify-center items-center py-10">
        <div className="bg-white shadow-lg rounded-lg p-6 w-full max-w-md">
          <h2 className="text-2xl font-bold text-center mb-4">Profile</h2>

          {message && (
            <p className="text-green-500 text-sm bg-green-100 p-2 rounded mb-3">
              {message}
            </p>
          )}

          {/* Name */}
          <div className="relative mb-3">
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="name" className={labelClasses(form.name)}>
              Name
            </label>
          </div>

          {/* Role (non-editable) */}
          <div className="relative mb-3">
            <input
              type="text"
              value={form.role}
              disabled
              className="peer border p-2 w-full rounded bg-gray-100"
            />
            <label className={labelClasses(form.role)}>Role</label>
          </div>

          {/* Upgrade button (only for End User) */}
          {form.role === "End User" && (
            <button
              onClick={handleUpgrade}
              className="w-full py-2 mb-3 bg-green-500 text-white rounded hover:bg-green-600"
            >
              Upgrade
            </button>
          )}

          {/* Category */}
          <div className="relative mb-3">
            <input
              type="text"
              name="category"
              value={form.category}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="category" className={labelClasses(form.category)}>
              Category In Interest
            </label>
          </div>

          {/* Language */}
          <div className="relative mb-3">
            <select
              name="language"
              value={form.language}
              onChange={handleChange}
              className="border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option>English</option>
              <option>Hindi</option>
              <option>French</option>
            </select>
          </div>

          {/* Profile Image Placeholder */}
          <div className="flex flex-col items-center mb-4">
            <div className="w-24 h-24 bg-gray-200 rounded-full mb-2 flex items-center justify-center text-gray-500">
              Profile Image
            </div>
            <button className="text-blue-600 text-sm hover:underline">
              Change
            </button>
          </div>

          {/* Save Button */}
          <button className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600">
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
}
