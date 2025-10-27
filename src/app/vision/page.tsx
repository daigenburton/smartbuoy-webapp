import Link from 'next/link';

export const metadata = {
    title: 'SmartBuoy - Our Vision'
}

export default function Vision() {
    return (

        <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-blue-50 via-white to-blue-100 text-center px-8 py-16">
            <h1 className="italic text-5xl font-semibold text-blue-700 mb-10 drop-shadow-sm">
                Smarter Fishing, Safer Oceans
            </h1>

            <div className="max-w-4xl text-lg text-gray-700 leading-relaxed text-left bg-white shadow-md rounded-2xl p-10 border border-blue-100">
                <p className="mb-5">
                    At SmartBuoy, we’re redefining the future of lobster fishing—making it
                    smarter, safer, and more sustainable. For decades, fishermen have faced
                    two major challenges: the risk of entanglement with marine wildlife and
                    the inability to monitor traps in real time.
                </p>

                <p className="mb-5">
                    Our vision is to bring technology and tradition together to protect
                    the ocean and empower those who depend on it. The SmartBuoy is a rugged,
                    buoy-mounted system that combines advanced sensors, wireless connectivity,
                    and cloud-based data services to give fishermen immediate insight into
                    trap conditions.
                </p>

                <p className="mb-5">
                    Each buoy tracks GPS location, water temperature, and tidal movement while
                    detecting signs of tampering or entanglement. Real-time data is transmitted
                    via LTE to our web platform, where users can monitor multiple buoys, receive
                    instant alerts, and locate traps with homing features. Built to endure harsh
                    marine environments, the SmartBuoy operates for up to seven days without
                    recharge and integrates seamlessly with existing gear.
                </p>

                <p>
                    By enhancing fishing efficiency and promoting marine conservation, SmartBuoy
                    is solving a problem that costs both livelihoods and marine life.
                </p>
            </div>

            <Link href="/home">
                <button className="mt-10 px-6 py-3 bg-blue-600 text-white rounded-full font-medium shadow hover:bg-blue-700 transition-all duration-300">
                    Back to Home
                </button>
                
            </Link>
        </div>
    );
}