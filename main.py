#!/usr/bin/env python3
import json
import argparse
import os
import requests
from datetime import datetime, timedelta
import heapq

def parse_time(time_str):
    return datetime.strptime(time_str, "%H:%M")

def time_to_minutes(time_str):
    dt = parse_time(time_str)
    return dt.hour * 60 + dt.minute

def minutes_to_time(minutes):
    hours = minutes // 60
    mins = minutes % 60
    return f"{hours:02d}:{mins:02d}"

def calculate_waiting_time(arrival_time, departure_time):
    arrival_mins = time_to_minutes(arrival_time)
    departure_mins = time_to_minutes(departure_time)
    if departure_mins >= arrival_mins:
        return departure_mins - arrival_mins
    else:
        return (24 * 60) - arrival_mins + departure_mins

def generate_summary_hf(route_data, api_key):
    if not api_key:
        return "Not generated"
    
    try:
        schedule = route_data.get('schedule', [])
        if not schedule:
            return "No routes available"
        
        total_time = route_data.get('value', 0)
        hours = total_time // 60
        minutes = total_time % 60
        
        prompt = f"Summarize this travel route: {len(schedule)} segments, total time {hours}h {minutes}m"
        
        headers = {"Authorization": f"Bearer {api_key}"}
        payload = {
            "inputs": prompt,
            "parameters": {"max_length": 60, "min_length": 20}
        }
        
        response = requests.post(
            "https://api-inference.huggingface.co/models/facebook/bart-large-cnn",
            headers=headers,
            json=payload,
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if isinstance(result, list) and len(result) > 0:
                return result[0].get('summary_text', 'Not generated')[:200]
        
        return "Not generated"
    except:
        return "Not generated"

def dijkstra_shortest_path(graph, start, end, criteria):
    # Get all nodes from graph
    all_nodes = set([start, end])
    for src in graph:
        all_nodes.add(src)
        for dst in graph[src]:
            all_nodes.add(dst)
    
    distances = {node: float('inf') for node in all_nodes}
    distances[start] = 0
    pq = [(0, start, [])]
    
    while pq:
        current_dist, current_node, path = heapq.heappop(pq)
        
        if current_node == end:
            return path, current_dist
        
        if current_dist > distances[current_node]:
            continue
        
        for neighbor, routes in graph.get(current_node, {}).items():
            for route in routes:
                if criteria == "Time":
                    weight = route['duration']
                elif criteria == "Cost":
                    weight = route['cost']
                else:  # Hops
                    weight = 1
                
                distance = current_dist + weight
                
                if distance < distances[neighbor]:
                    distances[neighbor] = distance
                    new_path = path + [route]
                    heapq.heappush(pq, (distance, neighbor, new_path))
    
    return [], float('inf')

def optimize_travel(routes_data, gen_trip_summary):
    api_key = os.getenv('HUGGINGFACE_API_KEY')
    results = {}
    
    for request in routes_data.get('requests', []):
        request_id = request['request_id']
        source = request['source']
        destination = request['destination']
        criteria = request['criteria']
        
        # Build graph
        graph = {}
        for route in routes_data.get('routes', []):
            src = route['source']
            dst = route['destination']
            
            if src not in graph:
                graph[src] = {}
            if dst not in graph[src]:
                graph[src][dst] = []
            
            duration = calculate_waiting_time("00:00", route['arrivalTime']) - calculate_waiting_time("00:00", route['departureTime'])
            if duration <= 0:
                duration += 24 * 60
            
            graph[src][dst].append({
                'source': src,
                'destination': dst,
                'mode': route['mode'],
                'departureTime': route['departureTime'],
                'arrivalTime': route['arrivalTime'],
                'cost': route['cost'],
                'duration': duration
            })
        
        # Find optimal path
        path, total_value = dijkstra_shortest_path(graph, source, destination, criteria)
        
        if not path:
            results[request_id] = {
                "schedule": [],
                "criteria": criteria,
                "value": 0,
                "travelSummary": "No routes available" if gen_trip_summary else "Not generated"
            }
        else:
            schedule = []
            total_time = 0
            total_cost = 0
            
            for i, route in enumerate(path):
                schedule.append({
                    "source": route['source'],
                    "destination": route['destination'],
                    "mode": route['mode'],
                    "departureTime": route['departureTime'],
                    "arrivalTime": route['arrivalTime'],
                    "cost": route['cost']
                })
                
                total_cost += route['cost']
                total_time += route['duration']
                
                # Add waiting time for connections
                if i < len(path) - 1:
                    waiting = calculate_waiting_time(route['arrivalTime'], path[i+1]['departureTime'])
                    total_time += waiting
            
            if criteria == "Time":
                value = total_time
            elif criteria == "Cost":
                value = total_cost
            else:  # Hops
                value = len(path)
            
            route_data = {
                "schedule": schedule,
                "criteria": criteria,
                "value": value
            }
            
            if gen_trip_summary:
                route_data["travelSummary"] = generate_summary_hf(route_data, api_key)
            else:
                route_data["travelSummary"] = "Not generated"
            
            results[request_id] = route_data
    
    return results

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--gen_trip_summary', type=str, required=True, choices=['true', 'false'])
    parser.add_argument('--input', type=str, required=True)
    args = parser.parse_args()
    
    gen_summary = args.gen_trip_summary.lower() == 'true'
    
    with open(args.input, 'r') as f:
        routes_data = json.load(f)
    
    results = optimize_travel(routes_data, gen_summary)
    print(json.dumps(results, indent=2))

if __name__ == "__main__":
    main()