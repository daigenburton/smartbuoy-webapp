import { NextRequest, NextResponse } from "next/server";

const BACKEND_API_BASE_URL =
  process.env.BACKEND_API_BASE_URL || "http://backend:8000";

type RouteContext = {
  params: Promise<{ buoyId: string }>;
};

export async function GET(_: NextRequest, context: RouteContext) {
  const { buoyId } = await context.params;

  try {
    const res = await fetch(`${BACKEND_API_BASE_URL}/shadow/${buoyId}`, {
      method: "GET",
      cache: "no-store",
    });

    const text = await res.text();

    return new NextResponse(text, {
      status: res.status,
      headers: {
        "Content-Type": res.headers.get("Content-Type") || "application/json",
      },
    });
  } catch (error) {
    console.error("Shadow GET proxy error:", error);

    return NextResponse.json(
      { error: "Failed to reach backend shadow endpoint." },
      { status: 502 }
    );
  }
}

export async function POST(req: NextRequest, context: RouteContext) {
  const { buoyId } = await context.params;

  try {
    const body = await req.text();

    const res = await fetch(`${BACKEND_API_BASE_URL}/shadow/${buoyId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body,
      cache: "no-store",
    });

    const text = await res.text();

    return new NextResponse(text, {
      status: res.status,
      headers: {
        "Content-Type": res.headers.get("Content-Type") || "application/json",
      },
    });
  } catch (error) {
    console.error("Shadow POST proxy error:", error);

    return NextResponse.json(
      { error: "Failed to reach backend shadow endpoint." },
      { status: 502 }
    );
  }
}

//mock test 
// import { NextRequest, NextResponse } from "next/server";

// let mockState: Record<string, any> = {
//   "buoy-1": {
//     deployed: false,
//     battery: 87,
//     buzzer: false,
//     led: false,
//   },
// };

// type RouteContext = {
//   params: Promise<{ buoyId: string }>;
// };

// // GET -> return fake shadow
// export async function GET(_: NextRequest, context: RouteContext) {
//   const { buoyId } = await context.params;

//   const state = mockState[buoyId] || {
//     deployed: false,
//     battery: 50,
//     buzzer: false,
//     led: false,
//   };

//   return NextResponse.json(state);
// }

// // POST -> update fake shadow
// export async function POST(req: NextRequest, context: RouteContext) {
//   const { buoyId } = await context.params;
//   const body = await req.json();

//   console.log("Mock POST received:", buoyId, body);

//   // merge updates into mock state
//   mockState[buoyId] = {
//     ...mockState[buoyId],
//     ...body,
//   };

//   return NextResponse.json({
//     status: "mock-updated",
//     buoyId,
//     newState: mockState[buoyId],
//   });
// }