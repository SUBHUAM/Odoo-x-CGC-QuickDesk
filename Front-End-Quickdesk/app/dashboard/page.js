"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Navbar from "../components/Navbar";
import AuthService from "../services/AuthService";

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState(null);
  const [search, setSearch] = useState("");
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  // --- Fetch current user ---
  useEffect(() => {
    const fetchUser = async () => {
      try {
        setLoading(true);
        const userData = await AuthService.getCurrentUser();
        setUser(userData);
      } catch (err) {
        router.push("/login");
      } finally {
        setLoading(false); // Hide loader
      }
    };
    fetchUser();
  }, [router]);

  // --- Fetch tickets on load ---
  useEffect(() => {
    const fetchTickets = async () => {
      try {
        setLoading(true);
        const data = await AuthService.getTickets(); // API call
        setTickets(data || []);
      } catch (err) {
        console.error("Error fetching tickets:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchTickets();
  }, []);

  const handleAskClick = () => {
    router.push("/create-ticket");
  };

  const handleSearch = () => {
    // Optional: filter tickets based on search
    const filtered = tickets.filter((ticket) =>
      ticket.name.toLowerCase().includes(search.toLowerCase())
    );
    setTickets(filtered);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar profile="1" />
      {/* Dashboard Content */}
      <div className="max-w-4xl mx-auto px-4 py-6">
        <h1 className="text-2xl font-bold mb-4">Dashboard</h1>

        {/* Search Bar with button */}
        <div className="flex items-center gap-2 mb-6">
          <input
            type="text"
            placeholder="Search tickets..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full border rounded p-2 focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSearch}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Search
          </button>
        </div>

        {/* Tickets List */}
        {loading ? (
          <p className="text-gray-500">Loading tickets...</p>
        ) : tickets.length === 0 ? (
          <p className="text-gray-500">No tickets available yet.</p>
        ) : (
          <div className="space-y-4">
            {tickets.map((ticket) => (
              <div
                key={ticket.id}
                className="bg-white p-4 rounded shadow hover:shadow-md transition"
              >
                <h2 className="text-lg font-semibold">{ticket.name}</h2>
                <p className="text-gray-600">{ticket.description}</p>
                <div className="flex justify-between items-center mt-2 text-sm text-gray-500">
                  <span>Status: {ticket.status}</span>
                  <span>
                    Updated: {new Date(ticket.updatedAt).toLocaleString()}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Floating Ask Button */}
      <button
        onClick={handleAskClick}
        className="fixed bottom-6 right-6 bg-blue-500 text-white px-4 py-2 rounded-full shadow hover:bg-blue-600"
      >
        Ask
      </button>
    </div>
  );
}
